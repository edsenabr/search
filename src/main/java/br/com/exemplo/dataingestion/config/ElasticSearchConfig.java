package br.com.exemplo.dataingestion.config;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import br.com.exemplo.dataingestion.bean.AWSRequestSigningApacheInterceptor;

@Configuration

public class ElasticSearchConfig {

    @Autowired
    private AWSCredentialsProvider awsCredentialsProvider;

    @Value("${spring.elasticsearch.rest.uris}")
    private String host;

    @Bean
    @Profile("aws")
    public RestHighLevelClient client() {
        AWS4Signer signer = new AWS4Signer();
        signer.setRegionName(Regions.getCurrentRegion().getName());
        String serviceName = "es";
        signer.setServiceName(serviceName);
        HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(serviceName, signer, awsCredentialsProvider);
        return new RestHighLevelClient(RestClient.builder(HttpHost.create(host)).setHttpClientConfigCallback(e -> e.addInterceptorLast(interceptor)));
    }
}
