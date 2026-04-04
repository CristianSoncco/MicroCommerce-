package com.microcommerce.order.exception.handler;

import com.microcommerce.order.dto.response.ErrorResponse;
import com.microcommerce.order.entity.Order.OrderStatus;
import com.microcommerce.order.exception.EmptyOrderException;
import com.microcommerce.order.exception.InvalidOrderStatusException;
import com.microcommerce.order.exception.OrderNotFoundException;
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
        when(request.getRequestURI()).thenReturn("/api/orders/order-001");
    }

    @Test
    @DisplayName("handleOrderNotFoundException - debe retornar 404")
    void handleOrderNotFoundException_ShouldReturn404() {
        // Given
        OrderNotFoundException ex = new OrderNotFoundException("order-001");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleOrderNotFoundException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).contains("order-001");
        assertThat(response.getBody().getPath()).isEqualTo("/api/orders/order-001");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("handleInvalidOrderStatusException - debe retornar 400")
    void handleInvalidOrderStatusException_ShouldReturn400() {
        // Given
        InvalidOrderStatusException ex = new InvalidOrderStatusException(
                OrderStatus.PENDING, OrderStatus.SHIPPED);

        // When
        ResponseEntity<ErrorResponse> response = handler.handleInvalidOrderStatusException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).contains("PENDING");
        assertThat(response.getBody().getMessage()).contains("SHIPPED");
    }

    @Test
    @DisplayName("handleEmptyOrderException - debe retornar 400")
    void handleEmptyOrderException_ShouldReturn400() {
        // Given
        EmptyOrderException ex = new EmptyOrderException();

        // When
        ResponseEntity<ErrorResponse> response = handler.handleEmptyOrderException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).contains("al menos un item");
    }

    @Test
    @DisplayName("handleIllegalArgumentException - debe retornar 400")
    void handleIllegalArgumentException_ShouldReturn400() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Argumento invalido");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Argumento invalido");
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
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).isEqualTo("Ha ocurrido un error interno en el servidor");
    }
}
