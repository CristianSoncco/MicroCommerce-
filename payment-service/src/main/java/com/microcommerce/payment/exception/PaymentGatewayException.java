package com.microcommerce.payment.exception;

/**
 * Exception thrown when communication with the payment gateway fails
 * Excepcion lanzada cuando la comunicacion con la pasarela de pago falla
 */
public class PaymentGatewayException extends RuntimeException {

    public PaymentGatewayException(String message) {
        super(message);
    }

    public PaymentGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
