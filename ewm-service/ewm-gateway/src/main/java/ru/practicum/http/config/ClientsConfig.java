package ru.practicum.http.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import ru.practicum.http.client.*;

@Configuration
@RequiredArgsConstructor
public class ClientsConfig {

    private final HttpServiceProxyFactory httpServiceProxyAdminFactory;

    private final HttpServiceProxyFactory httpServiceProxyPublicFactory;

    private final HttpServiceProxyFactory httpServiceProxyPrivateFactory;

    @Bean
    public CategoryHttpAdminClient adminClient() {
        return httpServiceProxyAdminFactory.createClient(CategoryHttpAdminClient.class);
    }

    @Bean
    public UserPrivateHttpClient userClient() {
        return httpServiceProxyAdminFactory.createClient(UserPrivateHttpClient.class);
    }

    @Bean
    public CategoryHttpPublicClient publicClient() {
        return httpServiceProxyPublicFactory.createClient(CategoryHttpPublicClient.class);
    }

    @Bean
    public EventPrivateHttpClient privateEventClient() {
        return httpServiceProxyPrivateFactory.createClient(EventPrivateHttpClient.class);
    }

    @Bean
    public ParticipationRequestHttpClient participationRequestClient() {
        return httpServiceProxyPrivateFactory.createClient(ParticipationRequestHttpClient.class);
    }

    @Bean
    public EventPublicHttpClient publicEventClient() {
        return httpServiceProxyPublicFactory.createClient(EventPublicHttpClient.class);
    }

    @Bean
    public EventAdminHttpClient eventAdminClient() {
        return httpServiceProxyAdminFactory.createClient(EventAdminHttpClient.class);
    }

    @Bean
    public CompilationAdminHttpClient compilationAdminClient() {
        return httpServiceProxyAdminFactory.createClient(CompilationAdminHttpClient.class);
    }

    @Bean
    public CompilationPublicHttpClient compilationPublicClient() {
        return httpServiceProxyPublicFactory.createClient(CompilationPublicHttpClient.class);
    }

    @Bean
    public CommentPrivateHttpClient commentPrivateHttpClient() {
        return httpServiceProxyPrivateFactory.createClient(CommentPrivateHttpClient.class);
    }

    @Bean
    public CommentPublicHttpClient commentPublicHttpClient() {
        return httpServiceProxyPublicFactory.createClient(CommentPublicHttpClient.class);
    }

    @Bean
    public CommentAdminHttpClient commentAdminHttpClient() {
        return httpServiceProxyAdminFactory.createClient(CommentAdminHttpClient.class);
    }

}

