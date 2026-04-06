package com.microcommerce.payment.exception;

/**
 * Exception thrown when a payment operation is invalid for the current state
 * Excepcion lanzada cuando una operacion de pago es invalida para el estado actual
 */
public class InvalidPaymentStateException extends RuntimeException {

    public InvalidPaymentStateException(String message) {
        super(message);
    }
}
