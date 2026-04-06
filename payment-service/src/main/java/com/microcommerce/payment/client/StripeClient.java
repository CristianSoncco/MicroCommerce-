package com.microcommerce.payment.client;

import com.microcommerce.payment.dto.request.StripePaymentIntentRequest;
import com.microcommerce.payment.dto.response.StripePaymentIntentResponse;
import com.microcommerce.payment.dto.response.StripeRefundResponse;
import com.microcommerce.payment.exception.PaymentGatewayException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;

/**
 * HTTP client for Stripe API communication using WebClient
 * Cliente HTTP para comunicacion con la API de Stripe usando WebClient
 */
@Component
public class StripeClient {

    private static final Logger log = LoggerFactory.getLogger(StripeClient.class);

    private final WebClient stripeWebClient;

    public StripeClient(WebClient stripeWebClient) {
        this.stripeWebClient = stripeWebClient;
    }

    /**
     * Create a PaymentIntent on Stripe with circuit breaker and retry
     * Crear un PaymentIntent en Stripe con circuit breaker y reintento
     *
     * @param request the payment intent request details
     * @return the Stripe PaymentIntent response
     */
    @CircuitBreaker(name = "stripePayment", fallbackMethod = "createPaymentIntentFallback")
    @Retry(name = "stripePayment")
    public StripePaymentIntentResponse createPaymentIntent(StripePaymentIntentRequest request) {
        log.info("Creando PaymentIntent en Stripe por monto: {} {}", request.getAmount(), request.getCurrency());

        try {
            StripePaymentIntentResponse response = stripeWebClient.post()
                    .uri("/payment_intents")
                    .body(BodyInserters.fromFormData("amount", String.valueOf(request.getAmount()))
                            .with("currency", request.getCurrency())
                            .with("payment_method", request.getPaymentMethodToken())
                            .with("description", request.getDescription() != null ? request.getDescription() : "")
                            .with("confirm", String.valueOf(request.isConfirm())))
                    .retrieve()
                    .bodyToMono(StripePaymentIntentResponse.class)
                    .block();

            log.info("PaymentIntent creado exitosamente: {}", response != null ? response.getId() : "null");
            return response;
        } catch (WebClientResponseException e) {
            log.error("Error en la API de Stripe: estado={}, cuerpo={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentGatewayException(
                    "Error al comunicarse con Stripe: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al comunicarse con Stripe: {}", e.getMessage());
            throw new PaymentGatewayException(
                    "Error inesperado al procesar el pago: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieve a PaymentIntent from Stripe
     * Obtener un PaymentIntent de Stripe
     *
     * @param paymentIntentId the Stripe PaymentIntent ID
     * @return the Stripe PaymentIntent response
     */
    @CircuitBreaker(name = "stripePayment", fallbackMethod = "getPaymentIntentFallback")
    @Retry(name = "stripePayment")
    public StripePaymentIntentResponse getPaymentIntent(String paymentIntentId) {
        log.info("Consultando PaymentIntent en Stripe: {}", paymentIntentId);

        try {
            return stripeWebClient.get()
                    .uri("/payment_intents/{id}", paymentIntentId)
                    .retrieve()
                    .bodyToMono(StripePaymentIntentResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error al consultar PaymentIntent: estado={}, cuerpo={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentGatewayException(
                    "Error al consultar el pago en Stripe: " + e.getMessage(), e);
        }
    }

    /**
     * Create a refund on Stripe
     * Crear un reembolso en Stripe
     *
     * @param paymentIntentId the Stripe PaymentIntent ID to refund
     * @param amount the amount to refund (null for full refund)
     * @param reason the reason for the refund
     * @return the Stripe Refund response
     */
    @CircuitBreaker(name = "stripePayment", fallbackMethod = "createRefundFallback")
    @Retry(name = "stripePayment")
    public StripeRefundResponse createRefund(String paymentIntentId, BigDecimal amount, String reason) {
        log.info("Creando reembolso en Stripe para PaymentIntent: {}", paymentIntentId);

        try {
            var formData = BodyInserters.fromFormData("payment_intent", paymentIntentId);

            if (amount != null) {
                long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();
                formData = formData.with("amount", String.valueOf(amountInCents));
            }

            if (reason != null && !reason.isBlank()) {
                formData = formData.with("reason", "requested_by_customer");
            }

            StripeRefundResponse response = stripeWebClient.post()
                    .uri("/refunds")
                    .body(formData)
                    .retrieve()
                    .bodyToMono(StripeRefundResponse.class)
                    .block();

            log.info("Reembolso creado exitosamente: {}", response != null ? response.getId() : "null");
            return response;
        } catch (WebClientResponseException e) {
            log.error("Error al crear reembolso: estado={}, cuerpo={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentGatewayException(
                    "Error al procesar el reembolso en Stripe: " + e.getMessage(), e);
        }
    }

    /**
     * Fallback for createPaymentIntent when circuit breaker is open
     * Fallback para createPaymentIntent cuando el circuit breaker esta abierto
     */
    public StripePaymentIntentResponse createPaymentIntentFallback(StripePaymentIntentRequest request, Throwable t) {
        log.error("Circuit breaker activado para createPaymentIntent. Error: {}", t.getMessage());
        throw new PaymentGatewayException(
                "El servicio de pagos no esta disponible en este momento. Intente nuevamente mas tarde.", t);
    }

    /**
     * Fallback for getPaymentIntent when circuit breaker is open
     * Fallback para getPaymentIntent cuando el circuit breaker esta abierto
     */
    public StripePaymentIntentResponse getPaymentIntentFallback(String paymentIntentId, Throwable t) {
        log.error("Circuit breaker activado para getPaymentIntent. Error: {}", t.getMessage());
        throw new PaymentGatewayException(
                "No se puede consultar el estado del pago en este momento. Intente nuevamente mas tarde.", t);
    }

    /**
     * Fallback for createRefund when circuit breaker is open
     * Fallback para createRefund cuando el circuit breaker esta abierto
     */
    public StripeRefundResponse createRefundFallback(String paymentIntentId, BigDecimal amount, String reason, Throwable t) {
        log.error("Circuit breaker activado para createRefund. Error: {}", t.getMessage());
        throw new PaymentGatewayException(
                "No se puede procesar el reembolso en este momento. Intente nuevamente mas tarde.", t);
    }
}
