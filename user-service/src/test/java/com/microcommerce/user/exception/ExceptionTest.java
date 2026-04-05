package com.microcommerce.user.exception;

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
    @DisplayName("UserNotFoundException - con ID - debe contener mensaje correcto")
    void userNotFoundException_WithId_ShouldContainCorrectMessage() {
        // When
        UserNotFoundException exception = new UserNotFoundException(42L);

        // Then
        assertThat(exception.getMessage()).isEqualTo("Usuario no encontrado con id: 42");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("UserNotFoundException - con mensaje custom - debe contener mensaje correcto")
    void userNotFoundException_WithCustomMessage_ShouldContainCorrectMessage() {
        // When
        UserNotFoundException exception = new UserNotFoundException("Usuario no encontrado con email: test@example.com");

        // Then
        assertThat(exception.getMessage()).isEqualTo("Usuario no encontrado con email: test@example.com");
    }

    @Test
    @DisplayName("UserAlreadyExistsException - con email - debe contener mensaje correcto")
    void userAlreadyExistsException_WithEmail_ShouldContainCorrectMessage() {
        // When
        UserAlreadyExistsException exception = new UserAlreadyExistsException("john@example.com");

        // Then
        assertThat(exception.getMessage()).isEqualTo("El usuario ya existe con el email: john@example.com");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("UserAlreadyExistsException - con causa - debe contener causa")
    void userAlreadyExistsException_WithCause_ShouldContainCause() {
        // Given
        RuntimeException cause = new RuntimeException("DB error");

        // When
        UserAlreadyExistsException exception = new UserAlreadyExistsException("Error duplicado", cause);

        // Then
        assertThat(exception.getMessage()).isEqualTo("Error duplicado");
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("InvalidCredentialsException - sin argumentos - debe contener mensaje por defecto")
    void invalidCredentialsException_Default_ShouldContainDefaultMessage() {
        // When
        InvalidCredentialsException exception = new InvalidCredentialsException();

        // Then
        assertThat(exception.getMessage()).isEqualTo("Credenciales invalidas");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("InvalidCredentialsException - con mensaje custom - debe contener mensaje correcto")
    void invalidCredentialsException_WithCustomMessage_ShouldContainCorrectMessage() {
        // When
        InvalidCredentialsException exception = new InvalidCredentialsException("Token expirado");

        // Then
        assertThat(exception.getMessage()).isEqualTo("Token expirado");
    }

    @Test
    @DisplayName("UserDisabledException - con email - debe contener mensaje correcto")
    void userDisabledException_WithEmail_ShouldContainCorrectMessage() {
        // When
        UserDisabledException exception = new UserDisabledException("disabled@example.com");

        // Then
        assertThat(exception.getMessage()).isEqualTo("La cuenta del usuario esta deshabilitada: disabled@example.com");
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
