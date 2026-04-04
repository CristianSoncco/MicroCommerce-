package com.microcommerce.order.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for ApiResponse
 * Tests unitarios para ApiResponse
 */
@DisplayName("ApiResponse Tests")
class ApiResponseTest {

    @Test
    @DisplayName("success con data - debe crear respuesta exitosa con mensaje por defecto")
    void success_WithData_ShouldCreateSuccessResponseWithDefaultMessage() {
        // When
        ApiResponse<String> response = ApiResponse.success("test data");

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Operacion exitosa");
        assertThat(response.getData()).isEqualTo("test data");
    }

    @Test
    @DisplayName("success con mensaje y data - debe crear respuesta con mensaje personalizado")
    void success_WithMessageAndData_ShouldCreateResponseWithCustomMessage() {
        // When
        ApiResponse<String> response = ApiResponse.success("Pedido creado", "test data");

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Pedido creado");
        assertThat(response.getData()).isEqualTo("test data");
    }

    @Test
    @DisplayName("error - debe crear respuesta de error sin datos")
    void error_ShouldCreateErrorResponseWithoutData() {
        // When
        ApiResponse<String> response = ApiResponse.error("Error ocurrido");

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Error ocurrido");
        assertThat(response.getData()).isNull();
    }

    @Test
    @DisplayName("constructor por defecto - debe crear instancia vacia")
    void defaultConstructor_ShouldCreateEmptyInstance() {
        // When
        ApiResponse<String> response = new ApiResponse<>();

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getData()).isNull();
    }

    @Test
    @DisplayName("constructor completo - debe crear instancia con todos los campos")
    void fullConstructor_ShouldCreateInstanceWithAllFields() {
        // When
        ApiResponse<Integer> response = new ApiResponse<>(true, "OK", 42);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("OK");
        assertThat(response.getData()).isEqualTo(42);
    }
}
