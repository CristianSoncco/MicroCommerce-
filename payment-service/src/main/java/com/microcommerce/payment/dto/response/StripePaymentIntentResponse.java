package com.microcommerce.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a Stripe PaymentIntent response
 * DTO representando una respuesta de PaymentIntent de Stripe
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StripePaymentIntentResponse {

    private String id;
    private String object;
    private Long amount;
    private String currency;
    private String status;
    private String clientSecret;
    private String paymentMethod;
    private String latestCharge;
    private String description;
    private Long created;

    /**
     * Check if the payment intent was successfully confirmed
     * Verificar si el intent de pago fue confirmado exitosamente
     */
    public boolean isSucceeded() {
        return "succeeded".equals(status);
    }

    /**
     * Check if the payment requires further action
     * Verificar si el pago requiere accion adicional
     */
    public boolean requiresAction() {
        return "requires_action".equals(status);
    }
}
