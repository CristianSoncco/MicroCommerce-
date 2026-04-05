package com.microcommerce.user.exception.handler;

import com.microcommerce.user.dto.response.ErrorResponse;
import com.microcommerce.user.exception.InvalidCredentialsException;
import com.microcommerce.user.exception.UserAlreadyExistsException;
import com.microcommerce.user.exception.UserDisabledException;
import com.microcommerce.user.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler
 * Tests unitarios para GlobalExceptionHandler
 */
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/users/1");
    }

    @Test
    @DisplayName("handleUserNotFoundException - debe retornar 404")
    void handleUserNotFoundException_ShouldReturn404() {
        // Given
        UserNotFoundException ex = new UserNotFoundException(1L);

        // When
        ResponseEntity<ErrorResponse> response = handler.handleUserNotFoundException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("No encontrado");
        assertThat(response.getBody().getMessage()).contains("1");
        assertThat(response.getBody().getPath()).isEqualTo("/api/users/1");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("handleUserAlreadyExistsException - debe retornar 409")
    void handleUserAlreadyExistsException_ShouldReturn409() {
        // Given
        UserAlreadyExistsException ex = new UserAlreadyExistsException("john@example.com");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleUserAlreadyExistsException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflicto");
        assertThat(response.getBody().getMessage()).contains("john@example.com");
    }

    @Test
    @DisplayName("handleInvalidCredentialsException - debe retornar 401")
    void handleInvalidCredentialsException_ShouldReturn401() {
        // Given
        InvalidCredentialsException ex = new InvalidCredentialsException();

        // When
        ResponseEntity<ErrorResponse> response = handler.handleInvalidCredentialsException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("No autorizado");
        assertThat(response.getBody().getMessage()).isEqualTo("Credenciales invalidas");
    }

    @Test
    @DisplayName("handleUserDisabledException - debe retornar 403")
    void handleUserDisabledException_ShouldReturn403() {
        // Given
        UserDisabledException ex = new UserDisabledException("disabled@example.com");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleUserDisabledException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
        assertThat(response.getBody().getError()).isEqualTo("Prohibido");
        assertThat(response.getBody().getMessage()).contains("disabled@example.com");
    }

    @Test
    @DisplayName("handleGenericException - debe retornar 500")
    void handleGenericException_ShouldReturn500() {
        // Given
        Exception ex = new Exception("Error inesperado");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Error interno del servidor");
        assertThat(response.getBody().getMessage()).isEqualTo("Ha ocurrido un error inesperado");
    }
}
