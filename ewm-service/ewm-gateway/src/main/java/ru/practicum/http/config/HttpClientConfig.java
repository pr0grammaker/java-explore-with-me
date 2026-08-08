package ru.practicum.http.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpClientConfig {

    @Value("${server.base-url-admin}")
    private String serverBaseUrlAdmin;

    @Bean
    RestClient adminRestClient(RestClient.Builder builder) {
        return builder
                .requestFactory(new HttpComponentsClientHttpRequestFactory()) // поддержка PATCH
                .baseUrl(serverBaseUrlAdmin)
                .build();
    }

    @Bean
    HttpServiceProxyFactory httpServiceProxyAdminFactory(RestClient adminRestClient) {
        return HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(adminRestClient))
                .build();
    }

    @Value("${server.base-url-public}")
    private String serverBaseUrlPublic;

    @Bean
    RestClient publicRestClient(RestClient.Builder builder) {
        return builder
                .requestFactory(new HttpComponentsClientHttpRequestFactory()) // поддержка PATCH
                .baseUrl(serverBaseUrlPublic)
                .build();
    }

    @Bean
    HttpServiceProxyFactory httpServiceProxyPublicFactory(RestClient publicRestClient) {
        return HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(publicRestClient))
                .build();
    }

    @Value("${server.base-url-private}")
    private String serverBaseUrlPrivate;

    @Bean
    RestClient privateRestClient(RestClient.Builder builder) {
        return builder
                .requestFactory(new HttpComponentsClientHttpRequestFactory()) // поддержка PATCH
                .baseUrl(serverBaseUrlPrivate)
                .build();
    }

    @Bean
    HttpServiceProxyFactory httpServiceProxyPrivateFactory(RestClient privateRestClient) {
        return HttpServiceProxyFactory.builder()
                .exchangeAdapter(RestClientAdapter.create(privateRestClient))
                .build();
    }


}
