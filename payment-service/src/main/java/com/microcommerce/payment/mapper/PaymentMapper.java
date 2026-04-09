package com.microcommerce.payment.mapper;

import com.microcommerce.payment.dto.request.PaymentRequest;
import com.microcommerce.payment.dto.response.PaymentResponse;
import com.microcommerce.payment.entity.Payment;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Payment entity and DTOs
 * Mapper para convertir entre entidad Payment y DTOs
 */
@Component
public class PaymentMapper {

    /**
     * Convert PaymentRequest to Payment entity
     * Convertir PaymentRequest a entidad Payment
     */
    public Payment toEntity(PaymentRequest request) {
        if (request == null) {
            return null;
        }

        return Payment.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .paymentMethod(request.getPaymentMethod())
                .description(request.getDescription())
                .build();
    }

    /**
     * Convert Payment entity to PaymentResponse
     * Convertir entidad Payment a PaymentResponse
     */
    public PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }

        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .stripePaymentIntentId(payment.getStripePaymentIntentId())
                .description(payment.getDescription())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
