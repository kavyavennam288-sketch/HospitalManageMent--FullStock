package com.medsync.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Value("${async.core-pool-size:2}")
    private int corePoolSize;

    @Value("${async.max-pool-size:5}")
    private int maxPoolSize;

    @Value("${async.queue-capacity:100}")
    private int queueCapacity;

    // This bean is picked up by @EnableAsync (in MedsyncApplication)
    // and used as the thread pool for all @Async methods.
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);    // threads always alive
        executor.setMaxPoolSize(maxPoolSize);       // max under load
        executor.setQueueCapacity(queueCapacity);  // queue before spinning up more threads
        executor.setThreadNamePrefix("MedSync-Async-");
        // Thread names show in logs: "MedSync-Async-1 sending email..."
        executor.initialize();
        return executor;
    }
}