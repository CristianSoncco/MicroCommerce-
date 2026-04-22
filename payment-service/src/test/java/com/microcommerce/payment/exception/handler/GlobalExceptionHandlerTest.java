package com.microcommerce.payment.exception.handler;

import com.microcommerce.payment.dto.response.ErrorResponse;
import com.microcommerce.payment.exception.InvalidPaymentStateException;
import com.microcommerce.payment.exception.PaymentAlreadyProcessedException;
import com.microcommerce.payment.exception.PaymentGatewayException;
import com.microcommerce.payment.exception.PaymentNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler
 * Tests unitarios para GlobalExceptionHandler
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/payments");
    }

    @Test
    @DisplayName("handlePaymentNotFoundException - debe retornar 404 NOT_FOUND")
    void handlePaymentNotFoundException_Returns404() {
        // Given
        PaymentNotFoundException ex = new PaymentNotFoundException(1L);

        // When
        ResponseEntity<ErrorResponse> response =
                globalExceptionHandler.handlePaymentNotFoundException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).contains("1");
        assertThat(response.getBody().getPath()).isEqualTo("/api/payments");
    }

    @Test
    @DisplayName("handlePaymentAlreadyProcessedException - debe retornar 409 CONFLICT")
    void handlePaymentAlreadyProcessedException_Returns409() {
        // Given
        PaymentAlreadyProcessedException ex = new PaymentAlreadyProcessedException(100L);

        // When
        ResponseEntity<ErrorResponse> response =
                globalExceptionHandler.handlePaymentAlreadyProcessedException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getMessage()).contains("100");
    }

    @Test
    @DisplayName("handleInvalidPaymentStateException - debe retornar 400 BAD_REQUEST")
    void handleInvalidPaymentStateException_Returns400() {
        // Given
        InvalidPaymentStateException ex =
                new InvalidPaymentStateException("Estado invalido para cancelacion");

        // When
        ResponseEntity<ErrorResponse> response =
                globalExceptionHandler.handleInvalidPaymentStateException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("Estado invalido");
    }

    @Test
    @DisplayName("handlePaymentGatewayException - debe retornar 503 SERVICE_UNAVAILABLE")
    void handlePaymentGatewayException_Returns503() {
        // Given
        PaymentGatewayException ex =
                new PaymentGatewayException("Error de conexion con Stripe");

        // When
        ResponseEntity<ErrorResponse> response =
                globalExceptionHandler.handlePaymentGatewayException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(503);
        assertThat(response.getBody().getMessage()).contains("Stripe");
    }

    @Test
    @DisplayName("handleValidationException - debe retornar 400 con detalles de validacion")
    void handleValidationException_Returns400WithDetails() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("paymentRequest", "amount",
                "El monto es obligatorio");
        FieldError fieldError2 = new FieldError("paymentRequest", "orderId",
                "El ID de la orden es obligatorio");

        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, bindingResult);

        // When
        ResponseEntity<ErrorResponse> response =
                globalExceptionHandler.handleValidationException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getDetails()).hasSize(2);
        assertThat(response.getBody().getDetails()).anyMatch(d -> d.contains("amount"));
        assertThat(response.getBody().getDetails()).anyMatch(d -> d.contains("orderId"));
    }

    @Test
    @DisplayName("handleGenericException - debe retornar 500 INTERNAL_SERVER_ERROR")
    void handleGenericException_Returns500() {
        // Given
        Exception ex = new RuntimeException("Error inesperado");

        // When
        ResponseEntity<ErrorResponse> response =
                globalExceptionHandler.handleGenericException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Error interno del servidor");
    }
}
