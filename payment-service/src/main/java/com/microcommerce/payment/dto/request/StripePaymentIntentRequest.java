package com.microcommerce.payment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a Stripe PaymentIntent creation request
 * DTO representando una solicitud de creacion de PaymentIntent en Stripe
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StripePaymentIntentRequest {

    private Long amount;
    private String currency;
    private String paymentMethodToken;
    private String description;
    private boolean confirm;
}
