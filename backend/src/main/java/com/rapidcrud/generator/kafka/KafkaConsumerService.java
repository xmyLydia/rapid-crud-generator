package com.rapidcrud.generator.kafka;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rapidcrud.generator.mongo.AuditLogDocument;
import com.rapidcrud.generator.mongo.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;


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

    private final AuditLogRepository auditLogRepository;

    private final ObjectMapper objectMapper;

    private final ElasticsearchClient elasticsearchClient;

    @KafkaListener(
            topics = "audit-log-topic",
            groupId = "audit-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(AuditLogEvent event, Acknowledgment ack) throws IOException {
        printLog(event);
        // Save to Mongo DB
        AuditLogDocument doc = new AuditLogDocument();
        doc.setAction(event.getAction());
        doc.setEntity(event.getEntity());
        doc.setPayload(event.getPayload());
        doc.setTimestamp(event.getTimestamp());

        auditLogRepository.save(doc);

        //Elastic search
        try {
            String json = objectMapper.writeValueAsString(event);
            Map<String, Object> jsonMap = objectMapper.readValue(json, new TypeReference<>() {});

            IndexRequest<Map<String, Object>> request = IndexRequest.of(builder ->
                    builder.index("audit-logs").document(jsonMap)
            );
            IndexResponse response = elasticsearchClient.index(request);
            log.info("✅ Indexed to Elasticsearch with ID: {}", response.id());
        } catch (Exception e) {
            log.error("❌ Elasticsearch index failed", e);
        }
        // ✅ Manually submit ACK after success
        ack.acknowledge();
        log.info("✅ Saved to MongoDB: {}", doc);
    }

    private static void printLog(AuditLogEvent event) {
        String threadName = Thread.currentThread().getName();
        int colorIndex = Math.abs(threadName.hashCode()) % COLORS.length;
        String color = COLORS[colorIndex];

        log.info(color + "📥 [{}] Received: {}" + RESET, threadName, event.getAction());
    }
}