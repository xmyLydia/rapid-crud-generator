package com.rapidcrud.generator.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
@RequiredArgsConstructor
public class AsyncExecutorConfig {

    private final LogExecutorProperties properties;

    /**
     * configure a thread pool to process async log task
     *
     * @return Executor thread pool instance
     */
    @Bean("logExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getCorePoolSize());
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        executor.setQueueCapacity(properties.getQueueCapacity());
        executor.setThreadNamePrefix("log-async-");
        executor.initialize();
        return executor;
    }
}

