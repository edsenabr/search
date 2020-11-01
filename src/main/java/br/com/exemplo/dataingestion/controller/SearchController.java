package br.com.exemplo.dataingestion.controller;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import br.com.exemplo.dataingestion.bean.AccountList;
import br.com.exemplo.dataingestion.service.SearchService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component()
@Slf4j
public class SearchController {

    @Autowired
    final ThreadPoolTaskScheduler scheduledExecutor = new ThreadPoolTaskScheduler();;

    @Value("${br.com.exemplo.dataingestion.threads:10}")
    private int numeroThreadsBusca;

    
    @Value("${br.com.exemplo.dataingestion.throttle:1}")
    private int intervaloBusca;

    @Value("${br.com.exemplo.dataingestion.query}")
    private String query;

    @Autowired
    private AccountList accountList;

    @Autowired
    private SearchService searchService;

    @PostConstruct
    private void configure() {
        scheduledExecutor.setPoolSize(numeroThreadsBusca);
        scheduledExecutor.setThreadNamePrefix("search-");
        scheduledExecutor.initialize();
    }

    @SneakyThrows
    public void search() {
        log.info("Inicializando buscas com {} threads", numeroThreadsBusca);
        for (int i = 0; i < numeroThreadsBusca;) {
            log.info("Criando worker {}", ++i);
            scheduledExecutor.scheduleWithFixedDelay(() -> {
                    searchService.search(query, accountList.get()).join();
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
