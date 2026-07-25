package ru.practicum.http.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import ru.practicum.http.client.EndpointHttpClient;

@Configuration
@RequiredArgsConstructor
public class ClientsConfig {

    private final HttpServiceProxyFactory factory;

    @Bean
    public EndpointHttpClient endpointHttpClient() {
        return factory.createClient(EndpointHttpClient.class);
    }

}

