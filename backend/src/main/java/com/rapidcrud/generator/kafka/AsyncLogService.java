package com.rapidcrud.generator.kafka;

import com.rapidcrud.generator.common.ExecutorRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncLogService {

    private final ExecutorRegistry executorRegistry;

    private final MeterRegistry meterRegistry;

    /**
     * submit async task，also record success, failure count and time out
     *
     * @param taskName task name
     * @param task     logic to execute
     */
    public void submit(String taskName, Runnable task) {
        executorRegistry.getExecutor(ExecutorRegistry.LOG_EXECUTOR_KEY).execute(() -> {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                task.run();
                //success counter + 1
                meterRegistry.counter(taskName + "_success_total").increment();
            } catch (Exception e) {
                //failure count + 1
                meterRegistry.counter(taskName + "_failure_total").increment();
                log.error("❌ Async log task failed", e);
            } finally {
                sample.stop(
                        Timer.builder(taskName + "_duration_seconds")
                                .description("Time taken for async task: " + taskName)
                                .publishPercentiles(0.5, 0.95, 0.99)
                                .publishPercentileHistogram()
                                .register(meterRegistry)
                );
            }
        });
    }
}
