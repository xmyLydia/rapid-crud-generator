package com.rapidcrud.generator.kafka;

import com.rapidcrud.generator.mongo.AuditDeadLetterDocument;
import com.rapidcrud.generator.mongo.AuditDeadLetterRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
@Slf4j
public class KafkaDLQConsumerService {

    private final AuditDeadLetterRepository deadLetterRepository;
    private final MeterRegistry meterRegistry;

    @KafkaListener(topics = "audit-log-dlt", groupId = "dlq-consumer-group")
    public void handleDLQ(AuditLogEvent event) {
        Instant start = Instant.now();
        log.warn("❌ [DLQ] Received: {}", event);

        AuditDeadLetterDocument doc = new AuditDeadLetterDocument();
        doc.setAction(event.getAction());
        doc.setEntity(event.getEntity());
        doc.setPayload(event.getPayload());
        doc.setTimestamp(event.getTimestamp());
        doc.setDeadLetteredAt(LocalDateTime.now());
        doc.setErrorMessage("Failed after max retry attempts");

        deadLetterRepository.save(doc);

        log.warn("✅ DLQ message saved to MongoDB (audit_dead_logs)");

        // ✅ 埋点：DLQ 消费成功次数 +1
        meterRegistry.counter("dlq_consumed_success_total").increment();

        // Record time cost
        Timer.builder("dlq_processing_duration_seconds")
                .description("Duration for processing DLQ messages")
                .register(meterRegistry)
                .record(Duration.between(start, Instant.now()));
    }
}
