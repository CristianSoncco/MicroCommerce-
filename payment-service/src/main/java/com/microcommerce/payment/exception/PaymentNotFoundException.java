package com.microcommerce.payment.exception;

/**
 * Exception thrown when a payment is not found
 * Excepcion lanzada cuando un pago no es encontrado
 */
public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(String message) {
        super(message);
    }

    public PaymentNotFoundException(Long id) {
        super("Pago no encontrado con ID: " + id);
    }
}
