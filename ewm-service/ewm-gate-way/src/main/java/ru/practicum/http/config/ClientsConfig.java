package ru.practicum.http.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import ru.practicum.http.client.CategoryHttpAdminClient;
import ru.practicum.http.client.CategoryHttpPublicClient;
import ru.practicum.http.client.UserPrivateHttpClient;

@Configuration
@RequiredArgsConstructor
public class ClientsConfig {

    private final HttpServiceProxyFactory httpServiceProxyAdminFactory;

    private final HttpServiceProxyFactory httpServiceProxyPublicFactory;

//    private final HttpServiceProxyFactory httpServiceProxyPrivateFactory;

    @Bean
    public CategoryHttpAdminClient adminClient() {
        return httpServiceProxyAdminFactory.createClient(CategoryHttpAdminClient.class);
    }

    @Bean
    public UserPrivateHttpClient  userClient() {
        return httpServiceProxyAdminFactory.createClient(UserPrivateHttpClient.class);
    }

    @Bean
    public CategoryHttpPublicClient publicClient() {
        return httpServiceProxyPublicFactory.createClient(CategoryHttpPublicClient.class);
    }

//    @Bean
//    public CategoryHttpPrivateClient privateClient() {
//        return httpServiceProxyPrivateFactory.createClient(CategoryHttpPrivateClient.class);
//    }

}

