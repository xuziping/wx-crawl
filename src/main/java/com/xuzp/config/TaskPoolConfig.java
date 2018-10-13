package com.xuzp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync
@Configuration
public class TaskPoolConfig {

    /**
     * 核心线程数
     */
    @Value("${spring.threadPool.corePoolSize}")
    private int corePoolSize;
    /**
     * 最大线程数
     */
    @Value("${spring.threadPool.maxPoolSize}")
    private int maxPoolSize;

    /**
     * 线程池维护线程所允许的空闲时间
     */
    @Value("${spring.threadPool.keepAliveSeconds}")
    private int keepAliveSeconds;

    /**
     * 队列最大长度
     */
    @Value("${spring.threadPool.queueCapacity}")
    private int queueCapacity;

    /**
     * 自定义异步线程池
     */
    @Bean(name = "spiderThreadAsyncPool")
    public AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(keepAliveSeconds);
        executor.setKeepAliveSeconds(queueCapacity);
        executor.setThreadNamePrefix("Anno-Executor");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
