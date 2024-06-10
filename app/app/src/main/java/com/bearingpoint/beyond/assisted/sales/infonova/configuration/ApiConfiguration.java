package com.bearingpoint.beyond.test-bpintegration.infonova.configuration;


import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.authorization.api.AuthorizationV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.authorization.client.RFC3339DateFormat;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.customers.api.CustomerAccountsV2Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.notifications.api.NotificationV2Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.ordering.api.OrderingV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.parties.api.PartiesV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.api.ServiceOrderingV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.templaterendering.api.TemplateRenderingV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.users.api.UsersV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productcatalog.api.ProductCatalogV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productinventory.api.ProductInventoryV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.api.ProductOrderingV1Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.api.TasksV2Api;
import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceinventory.api.ServiceInventoryV1Api;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.UnAuthenticatedServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

@Configuration
@Slf4j
public class ApiConfiguration {


    @Value("${infonovaBaseUrl}")
    private String infonovaBaseUrl;

    @Bean
    public ReactiveClientRegistrationRepository reactiveClientRegistrationRepository(OAuth2ClientProperties oAuth2ClientProperties) {
        List<ClientRegistration> clientRegistrations = new ArrayList<>();

        // because autoconfigure does not work for an unknown reason, here the ClientRegistrations are manually configured based on the application.yml
        oAuth2ClientProperties.getRegistration()
                .forEach((k, v) -> {
                    String tokenUri = oAuth2ClientProperties.getProvider().get(k).getTokenUri();
                    ClientRegistration clientRegistration = ClientRegistration
                            .withRegistrationId(k)
                            .tokenUri(tokenUri)
                            .clientId(v.getClientId())
                            .clientSecret(v.getClientSecret())
                            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                            .build();
                    clientRegistrations.add(clientRegistration);
                });

        return new InMemoryReactiveClientRegistrationRepository(clientRegistrations);
    }

    @Bean
    WebClient webClient(ReactiveClientRegistrationRepository reactiveClientRegistrationRepository) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                        reactiveClientRegistrationRepository,
                        new UnAuthenticatedServerOAuth2AuthorizedClientRepository());
        oauth.setDefaultClientRegistrationId("infonova");

        ObjectMapper mapper = createDefaultObjectMapper();

        int maxSize = 20 * 1024 * 1024; // 20 Mb

        ExchangeStrategies strategies = ExchangeStrategies
                .builder()
                .codecs(clientDefaultCodecsConfigurer -> {
                    clientDefaultCodecsConfigurer.defaultCodecs().maxInMemorySize(maxSize);
                    clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(mapper, MediaType.APPLICATION_JSON));
                    clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(mapper, MediaType.APPLICATION_JSON));
                }).build();
        return WebClient.builder()
                .clientConnector(createClientConnector())
                .exchangeStrategies(strategies)
                .filter(oauth)
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }


    private static ReactorClientHttpConnector createClientConnector() {
        // Solution for problem we have on GCP/Telus/Kong environment, where sometimes connections are dropped. This creates new connection on any WebClient call
        HttpClient httpClient = HttpClient.create(ConnectionProvider.newConnection());
        httpClient.keepAlive(false);

        return new ReactorClientHttpConnector(httpClient);
    }


    private static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.debug("Request header: {}={}", name, value)));
            return Mono.just(clientRequest);
        });
    }

    private static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("Response status: {}", clientResponse.statusCode());
            clientResponse.headers().asHttpHeaders().forEach((name, values) -> values.forEach(value -> log.debug("Response header: {}={}", name, value)));
            return logBody(clientResponse);
        });
    }

    private static Mono<ClientResponse> logBody(ClientResponse response) {
        if (response.statusCode() != null && (response.statusCode().is4xxClientError() || response.statusCode().is5xxServerError())) {
            return response.bodyToMono(String.class)
                    .flatMap(body -> {
                        log.error("Error Status: {} and Response body is  {}", response.statusCode(), body);
                        return Mono.just(response);
                    });
        } else {
            return Mono.just(response);
        }
    }

    @Bean
    WebClient webClientWithNullElements(ReactiveClientRegistrationRepository reactiveClientRegistrationRepository) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                        reactiveClientRegistrationRepository,
                        new UnAuthenticatedServerOAuth2AuthorizedClientRepository());
        oauth.setDefaultClientRegistrationId("infonova");

        ObjectMapper mapper = createWithNullElementsObjectMapper();

        ExchangeStrategies strategies = ExchangeStrategies
                .builder()
                .codecs(clientDefaultCodecsConfigurer -> {
                    clientDefaultCodecsConfigurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024);
                    clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(mapper, MediaType.APPLICATION_JSON));
                    clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(mapper, MediaType.APPLICATION_JSON));
                }).build();
        return WebClient.builder()
                .clientConnector(createClientConnector())
                .exchangeStrategies(strategies)
                .filter(oauth)
                .filter(logResponse())
                .build();
    }


    private ObjectMapper createDefaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(createDefaultDateFormat());
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configOverride(BigDecimal.class).setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.STRING));
        JsonNullableModule jnm = new JsonNullableModule();
        mapper.registerModule(jnm);
        return mapper;
    }

    private ObjectMapper createWithNullElementsObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(createDefaultDateFormat());
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.USE_DEFAULTS);
        mapper.configOverride(BigDecimal.class).setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.STRING));
        JsonNullableModule jnm = new JsonNullableModule();
        mapper.registerModule(jnm);
        return mapper;
    }

    public DateFormat createDefaultDateFormat() {
        DateFormat dateFormat = new RFC3339DateFormat();
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat;
    }

    @Bean
    public AuthorizationV1Api authorizationV1Api(WebClient webClient) {
        com.bearingpoint.beyond.test-bpintegration.infonova.api.client.authorization.client.ApiClient client = new com.bearingpoint.beyond.test-bpintegration.infonova.api.client.authorization.client.ApiClient(webClient, null, null);
        client.setBasePath(infonovaBaseUrl);
        return new AuthorizationV1Api(client);
    }

    @Bean
    public TasksV2Api tasksV2Api(WebClient webClient) {
        com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.client.ApiClient client = new com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.client.ApiClient(webClient, null, null);
        client.setBasePath(infonovaBaseUrl);
        return new TasksV2Api(client);
    }


    @Bean
    public TasksV2Api tasksV2ApiWithNullElements(WebClient webClientWithNullElements) {
        com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.client.ApiClient client = new com.bearingpoint.beyond.test-bpintegration.infonova.api.client.tasks.client.ApiClient(webClientWithNullElements, null, null);
        client.setBasePath(infonovaBaseUrl);
        return new TasksV2Api(client);
    }

    @Bean
    public ProductCatalogV1Api productCatalogV1Api(WebClient webClient) {
        com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productcatalog.client.ApiClient client = new com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productcatalog.client.ApiClient(webClient, null, null);
        client.setBasePath(infonovaBaseUrl);
        return new ProductCatalogV1Api(client);
    }

    @Bean
    public ProductInventoryV1Api productInventoryV1Api(WebClient webClient) {
        com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productinventory.client.ApiClient client = new com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productinventory.client.ApiClient(webClient, null, null);
        client.setBasePath(infonovaBaseUrl);
        return new ProductInventoryV1Api(client);
    }

    @Bean
    public ProductOrderingV1Api productOrderingV1Api(WebClient webClient) {
        com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.client.ApiClient client = new com.bearingpoint.beyond.test-bpintegration.infonova.api.client.productordering.client.ApiClient(webClient, null, null);
        client.setBasePath(infonovaBaseUrl);
        return new ProductOrderingV1Api(client);
    }

    @Bean
    public CustomerAccountsV2Api customerAccountsV2Api(WebClient webClient) {
        com.bearingpoint.beyond.test-bpintegration.infonova.api.client.customers.client.ApiClient client = new com.bearingpoint.beyond.test-bpintegration.infonova.api.client.customers.client.ApiClient(webClient, null, null);
        client.setBasePath(infonovaBaseUrl);
        return new CustomerAccountsV2Api(client);
    }

    @Bean
    public PartiesV1Api partiesV1Api(WebClient webClient) {
        com.bearingpoint.beyond.test-bpintegration.infonova.api.client.parties.client.ApiClient client = new com.bearingpoint.beyond.test-bpintegration.infonova.api.client.parties.client.ApiClient(webClient, null, null);
        client.setBasePath(infonovaBaseUrl);
        return new PartiesV1Api(client);
    }

    @Bean
    public UsersV1Api usersV1Api(WebClient webClient) {
        com.bearingpoint.beyond.test-bpintegration.infonova.api.client.users.client.ApiClient client = new com.bearingpoint.beyond.test-bpintegration.infonova.api.client.users.client.ApiClient(webClient, null, null);
        client.setBasePath(infonovaBaseUrl);
        return new UsersV1Api(client);
    }

    @Bean
    public OrderingV1Api orderingV1Api(WebClient webClient) {
        com.bearingpoint.beyond.test-bpintegration.infonova.api.client.ordering.client.ApiClient client = new com.bearingpoint.beyond.test-bpintegration.infonova.api.client.ordering.client.ApiClient(webClient, null, null);
        client.setBasePath(infonovaBaseUrl);
        return new OrderingV1Api(client);
    }

    @Bean
    public ServiceOrderingV1Api serviceOrderingV1Api(WebClient webClient) {
        com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.client.ApiClient client = new com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceordering.client.ApiClient(webClient, null, null);
        client.setBasePath(infonovaBaseUrl);
        return new ServiceOrderingV1Api(client);
    }

    @Bean
    public TemplateRenderingV1Api templateRenderingV1Api(WebClient webClient) {
        com.bearingpoint.beyond.test-bpintegration.infonova.api.client.templaterendering.client.ApiClient client = new com.bearingpoint.beyond.test-bpintegration.infonova.api.client.templaterendering.client.ApiClient(webClient, null, null);
        client.setBasePath(infonovaBaseUrl);
        return new TemplateRenderingV1Api(client);
    }

    @Bean
    public NotificationV2Api notificationV2Api(WebClient webClient) {
        com.bearingpoint.beyond.test-bpintegration.infonova.api.client.notifications.client.ApiClient client = new com.bearingpoint.beyond.test-bpintegration.infonova.api.client.notifications.client.ApiClient(webClient, null, null);
        client.setBasePath(infonovaBaseUrl);
        return new NotificationV2Api(client);
    }

    @Bean
    public ServiceInventoryV1Api serviceInventoryV1Api(WebClient webClient) {
        com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceinventory.client.ApiClient client = new com.bearingpoint.beyond.test-bpintegration.infonova.api.client.serviceinventory.client.ApiClient(webClient, null, null);
        client.setBasePath(infonovaBaseUrl);
        return new ServiceInventoryV1Api(client);
    }
}
