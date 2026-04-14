package com.portfolio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Async işlemler için thread pool konfigürasyonu.
 *
 * Şu an yalnızca audit log yazma işlemi async çalışır.
 *
 * Boyutlandırma kararı (portfolio — düşük trafik):
 *   core=2  : her zaman hazır bekleyen thread sayısı
 *   max=4   : anlık yoğunlukta açılabilecek max thread
 *   queue=200: thread'ler meşgulse bekleyen log sayısı
 *
 * RejectedExecutionHandler = CallerRunsPolicy:
 *   queue dolduğunda async olmak yerine çağıran thread çalıştırır.
 *   Log kaybetmek yerine hafif gecikme kabul edilir.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "auditExecutor")
    public Executor auditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("audit-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // Uygulama kapanırken queue'daki logların yazılmasını bekle (max 10s)
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        executor.initialize();
        return executor;
    }
}
