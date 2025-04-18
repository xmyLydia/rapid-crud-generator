package com.rapidcrud.generator.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "audit-log-topic", groupId = "audit-consumer-group")
    public void consume(AuditLogEvent event) {
        System.out.println("📥 Received Kafka log: " + event);
    }
}
