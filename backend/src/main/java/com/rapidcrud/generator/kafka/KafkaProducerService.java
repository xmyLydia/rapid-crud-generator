package com.rapidcrud.generator.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, AuditLogEvent> kafkaTemplate;

    public void sendLog(AuditLogEvent event) {
        kafkaTemplate.send("audit-log-topic", event);
        System.out.println("✅ Sent Kafka log: " + event);
    }
}
