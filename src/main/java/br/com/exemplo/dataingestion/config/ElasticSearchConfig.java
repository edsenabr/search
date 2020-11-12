package br.com.exemplo.dataingestion.config;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

import br.com.exemplo.dataingestion.bean.AWSRequestSigningApacheInterceptor;

@Configuration

public class ElasticSearchConfig {

    @Autowired
    private AWSCredentialsProvider awsCredentialsProvider;

    @Value("${spring.elasticsearch.rest.uris}")
    private String host;

    @Value("${spring.task.execution.pool.core-size:8}")
    private int numeroThreadsBusca;

    @Bean
    @Scope("singleton")
    @Profile({"aws", "remote"})
    public RestHighLevelClient client() {
        AWS4Signer signer = new AWS4Signer();
        DefaultAwsRegionProviderChain regionProviderChain = new DefaultAwsRegionProviderChain();
        signer.setRegionName(regionProviderChain.getRegion());
        String serviceName = "es";
        signer.setServiceName(serviceName);
        HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(serviceName, signer, awsCredentialsProvider);
        return new RestHighLevelClient(
            RestClient.builder(HttpHost.create(host))
                .setHttpClientConfigCallback(
                    new HttpClientConfigCallback() {
                        public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                            return httpClientBuilder
                            .addInterceptorLast(interceptor)
                            .setDefaultIOReactorConfig(
                                IOReactorConfig.custom()
                                    .setIoThreadCount(numeroThreadsBusca)
                                    .build()
                            );
                        }
                    }
                )
        );
    }
}
