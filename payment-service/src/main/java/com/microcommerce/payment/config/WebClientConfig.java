package com.microcommerce.payment.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * WebClient configuration for Stripe API communication
 * Configuracion de WebClient para comunicacion con la API de Stripe
 */
@Configuration
public class WebClientConfig {

    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.api.base-url}")
    private String stripeBaseUrl;

    @Bean
    public WebClient stripeWebClient() {
        return WebClient.builder()
                .baseUrl(stripeBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + stripeApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("Solicitud a Stripe: {} {}", clientRequest.method(), clientRequest.url());
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("Respuesta de Stripe: estado {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }
}
