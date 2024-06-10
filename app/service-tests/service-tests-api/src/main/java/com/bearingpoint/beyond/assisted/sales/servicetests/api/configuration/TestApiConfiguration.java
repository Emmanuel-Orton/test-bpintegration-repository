package com.bearingpoint.beyond.test-bpintegration.servicetests.api.configuration;

import com.bearingpoint.beyond.test-bpintegration.infonova.api.client.authorization.client.ApiClient;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.UnAuthenticatedServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Configuration
@PropertySource({"classpath:dev0x.properties"})
public class TestApiConfiguration {

    protected static final String event_client = "eventClient";

    @Bean
    public ReactiveClientRegistrationRepository reactiveClientRegistrationRepository(@Value("${tokenUri}") String tokenUri,
                                                                                     @Value("${eventClient}") String testClient,
                                                                                     @Value("${eventClientSecret}") String testClientSecret) {
        List<ClientRegistration> clientRegistrations = new ArrayList<>();

        ClientRegistration clientRegistration = ClientRegistration
                .withRegistrationId(event_client)
                .tokenUri(tokenUri)
                .clientId(testClient)
                .clientSecret(testClientSecret)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();

        clientRegistrations.add(clientRegistration);
        return new InMemoryReactiveClientRegistrationRepository(clientRegistrations);
    }

    @Bean
    public WebClient webClient(ReactiveClientRegistrationRepository clientRegistrations) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                        clientRegistrations,
                        new UnAuthenticatedServerOAuth2AuthorizedClientRepository());
        oauth.setDefaultClientRegistrationId(event_client);

        ExchangeStrategies strategies = getWebClientExchangeStrategies();

        return WebClient.builder()
                .filter(oauth)
                .exchangeStrategies(strategies)
                .build();
    }

    @Bean
    public WebClient noAuthWebClient() {
        ExchangeStrategies strategies = getWebClientExchangeStrategies();
        return WebClient.builder().exchangeStrategies(strategies).build();
    }

    private ExchangeStrategies getWebClientExchangeStrategies() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(createDefaultDateFormat());
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonNullableModule jnm = new JsonNullableModule();
        mapper.registerModule(jnm);

        return ExchangeStrategies
                .builder()
                .codecs(clientDefaultCodecsConfigurer -> {
                    clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(mapper, MediaType.APPLICATION_JSON));
                    clientDefaultCodecsConfigurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(mapper, MediaType.APPLICATION_JSON));
                }).build();
    }


    private DateFormat createDefaultDateFormat() {
        return new SimpleDateFormat(StdDateFormat.DATE_FORMAT_STR_ISO8601);
    }

    @Bean
    @Primary
    public ApiClient apiClient(@Value("${integrationBaseUrl}") String integrationBaseUrl, @Qualifier("webClient") WebClient webClient) {
        ApiClient client = new ApiClient(webClient, null, null);
        client.setBasePath(integrationBaseUrl);
        return client;
    }
}
