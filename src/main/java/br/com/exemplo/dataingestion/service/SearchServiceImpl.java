package br.com.exemplo.dataingestion.service;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

import br.com.exemplo.dataingestion.bean.AccountList;
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

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private AccountList accountList;

    private Counter itemsCounter = Metrics.globalRegistry.counter("search.items");
    private final Timer roundtripTimer = Timer.builder("search.request.roundrip")
        .publishPercentiles(0.5, 0.9, 0.95, 0.99)
        .percentilePrecision(0)
        .distributionStatisticExpiry(Duration.ofMinutes(1))
        .distributionStatisticBufferLength(32767)
        .publishPercentileHistogram()
        .register(Metrics.globalRegistry);

    private final Timer elasticSearchTimer = Timer.builder("search.request.elastic")
        .publishPercentiles(0.5, 0.9, 0.95, 0.99)
        .percentilePrecision(0)
        .distributionStatisticExpiry(Duration.ofMinutes(1))
        .distributionStatisticBufferLength(32767)
        .publishPercentileHistogram()
        .register(Metrics.globalRegistry);

    private final Timer totalMethodTimer = Timer.builder("search.request.total")
        .publishPercentiles(0.5, 0.9, 0.95, 0.99)
        .percentilePrecision(0)
        .distributionStatisticExpiry(Duration.ofMinutes(1))
        .distributionStatisticBufferLength(32767)
        .publishPercentileHistogram()
        .register(Metrics.globalRegistry);

    @SneakyThrows
    @Async
    @Override
    public CompletableFuture<Boolean> search(String query) {
        Timer.Sample methodTimer = Timer.start(Metrics.globalRegistry);
        String account = accountList.next();
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
        elasticSearchTimer.record(searchResponse.getTook().getMillis(), TimeUnit.MILLISECONDS);
        requestTimer.stop(roundtripTimer);

        long amount = searchResponse.getHits().getTotalHits().value;
        itemsCounter.increment(amount);

        log.info("Within {}ms, found {} items for account {}", searchResponse.getTook().getMillis(), amount, account);
        methodTimer.stop(totalMethodTimer);
        return CompletableFuture.completedFuture(Boolean.TRUE);
    }
}
