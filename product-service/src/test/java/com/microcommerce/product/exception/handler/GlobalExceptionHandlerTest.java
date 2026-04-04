package com.microcommerce.product.exception.handler;

import com.microcommerce.product.dto.response.ErrorResponse;
import com.microcommerce.product.exception.InsufficientStockException;
import com.microcommerce.product.exception.ProductAlreadyExistsException;
import com.microcommerce.product.exception.ProductNotFoundException;
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
        when(request.getRequestURI()).thenReturn("/api/products/1");
    }

    @Test
    @DisplayName("handleProductNotFoundException - debe retornar 404")
    void handleProductNotFoundException_ShouldReturn404() {
        // Given
        ProductNotFoundException ex = new ProductNotFoundException(1L);

        // When
        ResponseEntity<ErrorResponse> response = handler.handleProductNotFoundException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).contains("1");
        assertThat(response.getBody().getPath()).isEqualTo("/api/products/1");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("handleProductAlreadyExistsException - debe retornar 409")
    void handleProductAlreadyExistsException_ShouldReturn409() {
        // Given
        ProductAlreadyExistsException ex = new ProductAlreadyExistsException("Laptop HP");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleProductAlreadyExistsException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).contains("Laptop HP");
    }

    @Test
    @DisplayName("handleInsufficientStockException - debe retornar 400")
    void handleInsufficientStockException_ShouldReturn400() {
        // Given
        InsufficientStockException ex = new InsufficientStockException(1L, 20, 10);

        // When
        ResponseEntity<ErrorResponse> response = handler.handleInsufficientStockException(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).contains("Stock insuficiente");
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
}
