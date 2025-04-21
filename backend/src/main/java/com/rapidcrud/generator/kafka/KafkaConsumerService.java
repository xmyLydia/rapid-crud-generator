package com.rapidcrud.generator.kafka;

import com.rapidcrud.generator.mongo.AuditLogDocument;
import com.rapidcrud.generator.mongo.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

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

    @KafkaListener(
            topics = "audit-log-topic",
            groupId = "audit-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(AuditLogEvent event, Acknowledgment ack) {
        printLog(event);
        if ("user,product".equals(event.getEntity())) {
            throw new RuntimeException("💥 Simulated failure for retry demo");
        }
        // Save to Mongo DB
        AuditLogDocument doc = new AuditLogDocument();
        doc.setAction(event.getAction());
        doc.setEntity(event.getEntity());
        doc.setPayload(event.getPayload());
        doc.setTimestamp(event.getTimestamp());

        auditLogRepository.save(doc);

        // ✅ 成功处理后手动提交 offset
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