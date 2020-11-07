package br.com.exemplo.dataingestion.service;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope("prototype")
public class SearchServiceImpl implements SearchService {

    @Value("br.com.exemplo.dataingestion.query")
    private String query;

    @Value("${POD_NAME}") 
    String podName;

    AtomicLong records = new AtomicLong(0);

    @Autowired
    private RestHighLevelClient client;

    private Random random = new Random();

    private Counter itemsCounter, searchCounter;
    private Timer elasticSearchTimer, totalMethodTimer;
    private AtomicLong elasticSearchResponseTimeGauge = Metrics.globalRegistry.gauge("search.elastic.took", new AtomicLong(0));

    @PostConstruct
    private void init() {
        itemsCounter = Metrics.globalRegistry.counter("search.items", "Instance", podName);
        searchCounter = Metrics.globalRegistry.counter("search.requests", "Instance", podName);
        elasticSearchTimer = Metrics.globalRegistry.timer("search.request.roundrip", "Instance", podName);
        totalMethodTimer = Metrics.globalRegistry.timer("search.request.total", "Instance", podName);
    }

    @SneakyThrows
    // @Async("search")
    @Async
    @Override
    public CompletableFuture<Boolean> search(String query, String[] accounts) {
        Timer.Sample methodTimer = Timer.start(Metrics.globalRegistry);
        String account = accounts[random.nextInt(accounts.length)];
        SearchRequest searchRequest = new SearchRequest("extrato")
            .source(
                new SearchSourceBuilder()
                    .query(
                        QueryBuilders.wrapperQuery(
                            query.replace("{{account}}", account)
                        )
                    )
            ); 
        Timer.Sample requestTimer = Timer.start(Metrics.globalRegistry);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        elasticSearchResponseTimeGauge.set(searchResponse.getTook().getMillis());
        requestTimer.stop(elasticSearchTimer);

        long amount = searchResponse.getHits().getTotalHits().value;
        itemsCounter.increment(amount);
        searchCounter.increment();

        records.addAndGet(amount);
        log.debug("Within {}ms, found {} items for account {} with query {}", searchResponse.getTook().getMillis(), amount, account, query);
        methodTimer.stop(totalMethodTimer);
        return CompletableFuture.completedFuture(Boolean.TRUE);
    }
}
