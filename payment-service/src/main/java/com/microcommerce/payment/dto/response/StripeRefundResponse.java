package com.microcommerce.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a Stripe Refund response
 * DTO representando una respuesta de reembolso de Stripe
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StripeRefundResponse {

    private String id;
    private String object;
    private Long amount;
    private String currency;
    private String paymentIntent;
    private String status;
    private String reason;
    private Long created;

    /**
     * Check if the refund was successful
     * Verificar si el reembolso fue exitoso
     */
    public boolean isSucceeded() {
        return "succeeded".equals(status);
    }
}
