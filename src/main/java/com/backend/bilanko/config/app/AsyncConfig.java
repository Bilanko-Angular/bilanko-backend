package com.backend.bilanko.config.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration du pool de threads dédié aux tâches asynchrones (@Async).
 *
 * <p>Ce pool est utilisé notamment par {@code GlobalSearchService} pour lancer
 * les trois requêtes de recherche (produits, ventes, charges) en parallèle.</p>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Executor nommé "searchExecutor" utilisé par GlobalSearchService.
     *
     * <ul>
     *   <li>corePoolSize = 3  : un thread par source de données (product, sale, charge)</li>
     *   <li>maxPoolSize = 10  : montée en charge possible si plusieurs requêtes simultanées</li>
     *   <li>queueCapacity = 50 : file d'attente avant rejet</li>
     * </ul>
     */
    @Bean(name = "searchExecutor")
    public Executor searchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("search-async-");
        executor.initialize();
        return executor;
    }
}
