package com.rapidcrud.generator.kafka;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rapidcrud.generator.common.ExecutorRegistry;
import com.rapidcrud.generator.exception.ValidationException;
import com.rapidcrud.generator.mongo.AuditLogDocument;
import com.rapidcrud.generator.mongo.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {
    private static final String[] COLORS = {
            "\u001B[34m", //Blue
            "\u001B[32m", //Green
            "\u001B[35m", //Magenta
            "\u001B[36m", //Cyan
            "\u001B[33m", //Yellow
            "\u001B[37m", // Light Gray
    };

    private static final String RESET = "\u001B[0m";
    public static final String LOG_TASK_PREFIX = "log_task";

    private final AuditLogRepository auditLogRepository;

    private final ObjectMapper objectMapper;

    private final ElasticsearchClient elasticsearchClient;

    private final AsyncLogService asyncLogService;

    private final KafkaTemplate<String, AuditLogEvent> kafkaTemplate;

    private final ExecutorRegistry executorRegistry;

    @KafkaListener(
            topics = "audit-log-topic",
            groupId = "audit-consumer-group",
            containerFactory = "kafkaBatchListenerContainerFactory"
    )
    public void consumeBatch(List<AuditLogEvent> events, Acknowledgment ack) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(events.size());
        List<AuditLogEvent> failedEvents = Collections.synchronizedList(new ArrayList<>());
        ConcurrentHashMap<String, Boolean> processingMap = new ConcurrentHashMap<>();

        try {
            for (AuditLogEvent event : events) {
                printLog(event);
                String eventId = event.getAction() + "_" + event.getTimestamp();
                processingMap.put(eventId, false); // initialized as false (not done)

                executorRegistry.getExecutor(ExecutorRegistry.CONSUMER_EXECUTOR_KEY).execute(() -> {
                    try {
                        saveToMongo(event);
                        saveToElasticSearchAsync(event);
                        processingMap.put(eventId, true); // successfully processed
                    } catch (ValidationException ve) {
                        log.error("❌ Validation error: {}", event, ve);
                        failedEvents.add(event);
                    } catch (Exception ex) {
                        log.error("❌ Critical system error: {}", event, ex);
                        failedEvents.add(event);
                        throw new RuntimeException(ex);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // ✅ wait for all sub tasks completed with max 30s
            boolean finished = latch.await(30, TimeUnit.SECONDS);
            if (!finished) {
                log.error("🚨 Timeout! Not all consumer tasks finished. Remaining latch count: {}", latch.getCount());
                processingMap.forEach((id, done) -> {
                    if (!done) {
                        log.error("🚨 Unfinished Event ID: {}", id);
                    }
                });
                       }
            ack.acknowledge();

        } catch (Exception criticalEx) {
            log.error("❌ Critical failure during batch consumption", criticalEx);
            throw criticalEx;
        }

        if (!failedEvents.isEmpty()) {
            handleFailedEvents(failedEvents);
        }
    }

    private static void printLog(AuditLogEvent event) {
        String threadName = Thread.currentThread().getName();
        int colorIndex = Math.abs(threadName.hashCode()) % COLORS.length;
        String color = COLORS[colorIndex];

        log.info(color + "📥 [{}] Received: {}" + RESET, threadName, event.getAction());
    }


    private void saveToMongo(AuditLogEvent event) {
        if (isNull(event)) {
            throw new ValidationException("AuditLogEvent must not be null");
        }
        AuditLogDocument doc = new AuditLogDocument();
        doc.setAction(event.getAction());
        doc.setEntity(event.getEntity());
        doc.setPayload(event.getPayload());
        doc.setTimestamp(event.getTimestamp());

        auditLogRepository.save(doc);
        log.info("✅ Saved to MongoDB");
    }

    private void saveToElasticSearchAsync(AuditLogEvent event) {
        asyncLogService.submit(LOG_TASK_PREFIX, () -> {
            try {
                String json = objectMapper.writeValueAsString(event);
                Map<String, Object> jsonMap = objectMapper.readValue(json, new TypeReference<>() {
                });
                IndexRequest<Map<String, Object>> request = IndexRequest.of(b -> b.index("audit-logs").document(jsonMap));
                elasticsearchClient.index(request);
                log.info("✅ Indexed to Elasticsearch");
            } catch (Exception esEx) {
                log.error("❌ Failed to index to Elasticsearch", esEx);
            }
        });
    }

    private void handleFailedEvents(List<AuditLogEvent> failedEvents) {
        for (AuditLogEvent failed : failedEvents) {
            try {
                log.warn("⚠️ Sending failed event to audit-log-failed topic: {}", failed);
                kafkaTemplate.send("audit-log-failed", failed);
            } catch (Exception ex) {
                log.error("❌ Failed to handle failed event: {}", failed, ex);
            }
        }
    }
}