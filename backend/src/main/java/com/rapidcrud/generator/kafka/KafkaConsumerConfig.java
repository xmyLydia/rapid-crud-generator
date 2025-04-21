package com.rapidcrud.generator.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.BackOffExecution;
import java.util.function.BiConsumer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, AuditLogEvent> kafkaTemplate) {
        // 🌀 Retry up to 3 times with jitter (2s ± 1s)
        BackOff backOff = new BackOff() {
            private final Random random = new Random();
            private final int maxRetries = 3;
            @Override
            public BackOffExecution start() {
                return new BackOffExecution() {
                    int attempt = 0;
                    @Override
                    public long nextBackOff() {
                        if (attempt++ >= maxRetries) return BackOffExecution.STOP;
                        return 2000 + random.nextInt(1000); // jitter
                    }
                };
            }
        };

        ConsumerRecordRecoverer recoverer = (record, ex) -> {
            log.error("❌ Reached max retry. Sending to DLQ. Record: {}", record.value());
            kafkaTemplate.send("audit-log-dlt", record.key().toString(), (AuditLogEvent) record.value());
        };

        return new DefaultErrorHandler(recoverer, backOff);
    }


    @Bean
    public ConsumerFactory<String, AuditLogEvent> consumerFactory() {
        StringDeserializer keyDeserializer = new StringDeserializer();

        JsonDeserializer<AuditLogEvent> valueDeserializer = new JsonDeserializer<>(AuditLogEvent.class);
        valueDeserializer.addTrustedPackages("*");

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "audit-consumer-group");

        return new DefaultKafkaConsumerFactory<>(
                props,
                keyDeserializer,  // ✅ 显式传入 key deserializer
                valueDeserializer               // ✅ 显式传入 value deserializer 实例
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AuditLogEvent> kafkaListenerContainerFactory(KafkaTemplate<String, AuditLogEvent> kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, AuditLogEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // ✅ 设置手动 ACK 模式
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // ✅ 设置消费线程并发数（如果需要）
        factory.setConcurrency(3);

        // ⬇️ 加入 error handler
        factory.setCommonErrorHandler(errorHandler(kafkaTemplate));

        return factory;
    }
}
