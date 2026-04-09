package com.microcommerce.order.exception;

import com.microcommerce.order.entity.Order.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for custom exception classes
 * Tests unitarios para clases de excepcion personalizadas
 */
@DisplayName("Exception Classes Tests")
class ExceptionTest {

    @Test
    @DisplayName("OrderNotFoundException - con ID - debe contener mensaje correcto")
    void orderNotFoundException_WithId_ShouldContainCorrectMessage() {
        // When
        OrderNotFoundException exception = new OrderNotFoundException("order-001");

        // Then
        assertThat(exception.getMessage()).isEqualTo("Pedido no encontrado con id: order-001");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("EmptyOrderException - debe contener mensaje correcto")
    void emptyOrderException_ShouldContainCorrectMessage() {
        // When
        EmptyOrderException exception = new EmptyOrderException();

        // Then
        assertThat(exception.getMessage()).isEqualTo("El pedido debe contener al menos un item");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("InvalidOrderStatusException - con estados - debe contener mensaje correcto")
    void invalidOrderStatusException_WithStatuses_ShouldContainCorrectMessage() {
        // When
        InvalidOrderStatusException exception = new InvalidOrderStatusException(
                OrderStatus.PENDING, OrderStatus.SHIPPED);

        // Then
        assertThat(exception.getMessage())
                .isEqualTo("Transicion de estado invalida de PENDING a SHIPPED");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("InvalidOrderStatusException - estados terminales - debe contener mensaje correcto")
    void invalidOrderStatusException_TerminalStates_ShouldContainCorrectMessage() {
        // When
        InvalidOrderStatusException exception = new InvalidOrderStatusException(
                OrderStatus.DELIVERED, OrderStatus.CANCELLED);

        // Then
        assertThat(exception.getMessage())
                .contains("DELIVERED")
                .contains("CANCELLED");
    }
}
