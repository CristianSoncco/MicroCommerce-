package com.microcommerce.product.exception;

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
    @DisplayName("ProductNotFoundException - con ID - debe contener mensaje correcto")
    void productNotFoundException_WithId_ShouldContainCorrectMessage() {
        // When
        ProductNotFoundException exception = new ProductNotFoundException(42L);

        // Then
        assertThat(exception.getMessage()).isEqualTo("Product not found with id: 42");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("ProductNotFoundException - con mensaje custom - debe contener mensaje correcto")
    void productNotFoundException_WithCustomMessage_ShouldContainCorrectMessage() {
        // When
        ProductNotFoundException exception = new ProductNotFoundException("Producto personalizado no encontrado");

        // Then
        assertThat(exception.getMessage()).isEqualTo("Producto personalizado no encontrado");
    }

    @Test
    @DisplayName("ProductAlreadyExistsException - con nombre - debe contener mensaje correcto")
    void productAlreadyExistsException_WithName_ShouldContainCorrectMessage() {
        // When
        ProductAlreadyExistsException exception = new ProductAlreadyExistsException("Laptop HP");

        // Then
        assertThat(exception.getMessage()).isEqualTo("El producto ya existe con el nombre: Laptop HP");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("ProductAlreadyExistsException - con causa - debe contener causa")
    void productAlreadyExistsException_WithCause_ShouldContainCause() {
        // Given
        RuntimeException cause = new RuntimeException("DB error");

        // When
        ProductAlreadyExistsException exception = new ProductAlreadyExistsException("Error duplicado", cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo("Error duplicado");
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("InsufficientStockException - con ID - debe contener mensaje correcto")
    void insufficientStockException_WithId_ShouldContainCorrectMessage() {
        // When
        InsufficientStockException exception = new InsufficientStockException(1L);

        // Then
        assertThat(exception.getMessage()).isEqualTo("Stock insuficiente para el producto con id: 1");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("InsufficientStockException - con detalles de stock - debe contener cantidades")
    void insufficientStockException_WithStockDetails_ShouldContainQuantities() {
        // When
        InsufficientStockException exception = new InsufficientStockException(5L, 20, 10);

        // Then
        assertThat(exception.getMessage())
                .contains("Stock insuficiente")
                .contains("5")
                .contains("20")
                .contains("10");
    }

    @Test
    @DisplayName("InsufficientStockException - con mensaje custom - debe contener mensaje correcto")
    void insufficientStockException_WithCustomMessage_ShouldContainCorrectMessage() {
        // When
        InsufficientStockException exception = new InsufficientStockException("Stock personalizado insuficiente");

        // Then
        assertThat(exception.getMessage()).isEqualTo("Stock personalizado insuficiente");
    }
}
