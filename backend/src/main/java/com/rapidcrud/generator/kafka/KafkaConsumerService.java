package com.rapidcrud.generator.kafka;

import com.rapidcrud.generator.mongo.AuditLogDocument;
import com.rapidcrud.generator.mongo.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final AuditLogRepository auditLogRepository;

    @KafkaListener(topics = "audit-log-topic", groupId = "audit-consumer-group")
    public void consume(AuditLogEvent event) {
        System.out.println("📥 Received Kafka log: " + event);

        // Save to Mongo DB
        AuditLogDocument doc = new AuditLogDocument();
        doc.setAction(event.getAction());
        doc.setEntity(event.getEntity());
        doc.setPayload(event.getPayload());
        doc.setTimestamp(event.getTimestamp());

        auditLogRepository.save(doc);

        System.out.println("✅ Saved to MongoDB: " + doc);
    }
}