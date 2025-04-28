package com.rapidcrud.generator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ConsumerTaskExecutorConfig {

    @Bean("consumerTaskExecutor")
    public ThreadPoolTaskExecutor consumerTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);    // number of core thread
        executor.setMaxPoolSize(16);    // max number of thread
        executor.setQueueCapacity(500); // queue capacity
        executor.setThreadNamePrefix("consumer-task-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
