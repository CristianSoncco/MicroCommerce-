package com.microcommerce.payment.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Payment Service custom exceptions
 * Tests unitarios para excepciones personalizadas de Payment Service
 */
@DisplayName("Exception Tests")
class ExceptionTest {

    // ==================== PaymentNotFoundException Tests ====================

    @Test
    @DisplayName("PaymentNotFoundException - con mensaje - debe contener mensaje correcto")
    void paymentNotFoundException_WithMessage_ContainsCorrectMessage() {
        // When
        PaymentNotFoundException exception = new PaymentNotFoundException("Pago no encontrado");

        // Then
        assertThat(exception.getMessage()).isEqualTo("Pago no encontrado");
    }

    @Test
    @DisplayName("PaymentNotFoundException - con ID - debe contener ID en mensaje")
    void paymentNotFoundException_WithId_ContainsIdInMessage() {
        // When
        PaymentNotFoundException exception = new PaymentNotFoundException(42L);

        // Then
        assertThat(exception.getMessage()).contains("42");
    }

    // ==================== PaymentAlreadyProcessedException Tests ====================

    @Test
    @DisplayName("PaymentAlreadyProcessedException - con mensaje - debe contener mensaje correcto")
    void paymentAlreadyProcessedException_WithMessage_ContainsCorrectMessage() {
        // When
        PaymentAlreadyProcessedException exception =
                new PaymentAlreadyProcessedException("Pago duplicado");

        // Then
        assertThat(exception.getMessage()).isEqualTo("Pago duplicado");
    }

    @Test
    @DisplayName("PaymentAlreadyProcessedException - con orderId - debe contener orderId en mensaje")
    void paymentAlreadyProcessedException_WithOrderId_ContainsOrderIdInMessage() {
        // When
        PaymentAlreadyProcessedException exception =
                new PaymentAlreadyProcessedException("100");

        // Then
        assertThat(exception.getMessage()).contains("100");
    }

    // ==================== InvalidPaymentStateException Tests ====================

    @Test
    @DisplayName("InvalidPaymentStateException - con mensaje - debe contener mensaje correcto")
    void invalidPaymentStateException_WithMessage_ContainsCorrectMessage() {
        // When
        InvalidPaymentStateException exception =
                new InvalidPaymentStateException("Estado invalido para operacion");

        // Then
        assertThat(exception.getMessage()).isEqualTo("Estado invalido para operacion");
    }

    // ==================== PaymentGatewayException Tests ====================

    @Test
    @DisplayName("PaymentGatewayException - con mensaje - debe contener mensaje correcto")
    void paymentGatewayException_WithMessage_ContainsCorrectMessage() {
        // When
        PaymentGatewayException exception =
                new PaymentGatewayException("Error en Stripe");

        // Then
        assertThat(exception.getMessage()).isEqualTo("Error en Stripe");
    }

    @Test
    @DisplayName("PaymentGatewayException - con causa - debe contener causa correcta")
    void paymentGatewayException_WithCause_ContainsCorrectCause() {
        // Given
        RuntimeException cause = new RuntimeException("Connection timeout");

        // When
        PaymentGatewayException exception =
                new PaymentGatewayException("Error en Stripe", cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo("Error en Stripe");
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Connection timeout");
    }
}
