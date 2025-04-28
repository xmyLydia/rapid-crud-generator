package com.rapidcrud.generator.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Slf4j
@Component
public class ExecutorRegistry {

    private static final String LOG_EXECUTOR_NAME = "logExecutor";
    private static final String CONSUMER_TASK_EXECUTOR_NAME = "consumerTaskExecutor";
    public static final String LOG_EXECUTOR_KEY = "log";
    public static final String CONSUMER_EXECUTOR_KEY = "consumer";
    private final Map<String, Executor> executors = new ConcurrentHashMap<>();

    public ExecutorRegistry(
            @Qualifier(LOG_EXECUTOR_NAME) ThreadPoolTaskExecutor logExecutor,
            @Qualifier(CONSUMER_TASK_EXECUTOR_NAME) ThreadPoolTaskExecutor consumerTaskExecutor
    ) {
        log.info("🔵 Creating ExecutorRegistry, logExecutor = {}, consumerExecutor = {}", logExecutor, consumerTaskExecutor);
        executors.put(LOG_EXECUTOR_KEY, logExecutor);
        executors.put(CONSUMER_EXECUTOR_KEY, consumerTaskExecutor);
    }

    public ThreadPoolTaskExecutor getExecutor(String name) {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) executors.get(name);
        if (executor == null) {
            throw new IllegalArgumentException("❌ No executor found for name: " + name);
        }
        return executor;
    }
}
