package com.rapidcrud.generator.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, AuditLogEvent> kafkaTemplate;

    public void sendLog(AuditLogEvent event) {
        // Using UUID as message key to ensure random partition distribution
        String randomKey = UUID.randomUUID().toString();
        kafkaTemplate.send("audit-log-topic", randomKey, event);

        System.out.println("✅ Sent Kafka log: " + event);
    }
}
