package com.microcommerce.payment.exception;

/**
 * Exception thrown when a payment has already been processed
 * Excepcion lanzada cuando un pago ya ha sido procesado
 */
public class PaymentAlreadyProcessedException extends RuntimeException {

    public PaymentAlreadyProcessedException(String message) {
        super(message);
    }

    public PaymentAlreadyProcessedException(Long orderId) {
        super("Ya existe un pago completado o en proceso para la orden: " + orderId);
    }
}
