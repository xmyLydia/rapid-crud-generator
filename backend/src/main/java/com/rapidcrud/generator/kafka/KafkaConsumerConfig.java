package com.rapidcrud.generator.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
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
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // manual submit offset
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100); // ✅ pull 100 messages per batch

        return new DefaultKafkaConsumerFactory<>(
                props,
                keyDeserializer,
                valueDeserializer
        );
    }

    @Bean(name = "kafkaBatchListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, AuditLogEvent> kafkaBatchListenerContainerFactory(KafkaTemplate<String, AuditLogEvent> kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, AuditLogEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // ✅ enable batch consume
        factory.setBatchListener(true);

        // ✅ set manual ACK mode (submit after processed)
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // ✅ set consumer thread concurrency number
        factory.setConcurrency(3);

        // ⬇️ add error handler
        factory.setCommonErrorHandler(errorHandler(kafkaTemplate));

        return factory;
    }
}
