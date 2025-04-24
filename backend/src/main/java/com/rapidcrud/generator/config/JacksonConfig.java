package com.rapidcrud.generator.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder
                .modules(new JavaTimeModule()) // ✅ support Java 8 time class
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // ✅ output string instead of timestamp
                .simpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); // ✅ control the format
    }
}

