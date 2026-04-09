package com.microcommerce.payment.entity;

/**
 * Enum representing the status of a payment
 * Enum representando el estado de un pago
 */
public enum PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED,
    CANCELLED
}
