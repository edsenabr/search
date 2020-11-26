package br.com.exemplo.dataingestion.controller;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import br.com.exemplo.dataingestion.service.SearchService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component()
@Slf4j
public class SearchController {

    @Autowired
    ThreadPoolTaskScheduler scheduledExecutor;

    @Value("${spring.task.scheduling.pool.size:8}")
    private int numeroThreadsBusca;

    @Value("${br.com.exemplo.dataingestion.throttle:1}")
    private int intervaloBusca;

    @Value("${br.com.exemplo.dataingestion.query}")
    private String query;

    @Autowired
    private SearchService searchService;

    @SneakyThrows
    public void search() {
        
        log.info("Inicializando buscas com {} threads", numeroThreadsBusca);
        // while(!Thread.currentThread().isInterrupted()){  
        //     searchService.search(query);
        // }         
        for (int i = 0; i < numeroThreadsBusca;) {
            log.info("Criando worker {}", ++i);
            scheduledExecutor.scheduleWithFixedDelay(() -> {
                    searchService.search(query);
                }, 
                intervaloBusca
            );
        }
        log.info("Workers criados.");
    }

    @PreDestroy
    void shutdown(){
        scheduledExecutor.shutdown();
    }
}
