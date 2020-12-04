package br.com.exemplo.dataingestion.config;

import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import br.com.exemplo.dataingestion.bean.AWSRequestSigningApacheInterceptor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ElasticSearchConfig {

    @Autowired
    private AWSCredentialsProvider awsCredentialsProvider;

    @Value("${spring.elasticsearch.rest.uris}")
    private String host;

    @Value("${spring.task.scheduling.pool.size:8}")
    private int numeroThreadsBusca;

    @Autowired
    private Environment environment;

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
                        @SneakyThrows
                        public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                            HttpAsyncClientBuilder builder =  httpClientBuilder
                            .addInterceptorLast(interceptor)
                            .setMaxConnPerRoute(numeroThreadsBusca)
                            .setMaxConnTotal(numeroThreadsBusca)
                            .setDefaultIOReactorConfig(
                                IOReactorConfig.custom()
                                    .setIoThreadCount(numeroThreadsBusca)
                                    .build()
                            );
                            if (environment.acceptsProfiles(Profiles.of("remote"))) {
                                log.error("Autenticação de Host SSL desabilitada");

                                TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                        return null;
                                    }
        
                                    @Override
                                    public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
                                        throws CertificateException {}
        
                                    @Override
                                    public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
                                        throws CertificateException {}
                                    }
                                };

                                SSLContext context = SSLContext.getInstance("SSL");
                                context.init(null, trustAllCerts, null);
                                builder.setSSLContext(context);

                                builder.setSSLHostnameVerifier(new HostnameVerifier() {
                                    @Override
                                    public boolean verify(String hostname, SSLSession session) {
                                        return true;
                                    }
                                });
                            }
                            return builder;
                        }
                    }
                )
        );
    }
}
