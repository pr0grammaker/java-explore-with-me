package ru.practicum.http.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpClientConfig {

    @Value("${server.base-url}")
    private String serverBaseUrl;

    @Bean
    RestClient restClient(RestClient.Builder builder) {
        return builder
                .baseUrl(serverBaseUrl)
                .build();
    }

    @Bean
    HttpServiceProxyFactory httpServiceProxyFactory(RestClient restClient) {
        return HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(restClient))
                .build();
    }
}
