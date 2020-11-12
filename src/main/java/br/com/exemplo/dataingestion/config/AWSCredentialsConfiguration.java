package br.com.exemplo.dataingestion.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.WebIdentityTokenCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
public class AWSCredentialsConfiguration {

    @Bean(name="awsCredentialsProvider")
    @Profile("remote")
    public AWSCredentialsProvider getProfileCredentialsProvider() {
        return new ProfileCredentialsProvider();
    }

    @Bean(name="awsCredentialsProvider")
    @Profile("aws")
    public AWSCredentialsProvider getWebIdentityTokenCredentialsProvider() {
        return WebIdentityTokenCredentialsProvider.builder()
        .roleArn(System.getenv("AWS_ROLE_ARN"))
        .webIdentityTokenFile(System.getenv("AWS_WEB_IDENTITY_TOKEN_FILE"))
        .build();
    }
}
