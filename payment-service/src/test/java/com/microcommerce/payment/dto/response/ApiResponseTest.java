package com.microcommerce.payment.dto.response;

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
    @DisplayName("success con data - debe retornar success true con data y mensaje por defecto")
    void success_WithData_ReturnsSuccessTrueWithDefaultMessage() {
        // Given
        String data = "test data";

        // When
        ApiResponse<String> response = ApiResponse.success(data);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Operacion exitosa");
        assertThat(response.getData()).isEqualTo("test data");
    }

    @Test
    @DisplayName("success con mensaje y data - debe retornar success true con mensaje personalizado")
    void success_WithMessageAndData_ReturnsSuccessTrueWithCustomMessage() {
        // Given
        String message = "Pago procesado correctamente";
        PaymentResponse data = PaymentResponse.builder().id(1L).build();

        // When
        ApiResponse<PaymentResponse> response = ApiResponse.success(message, data);

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Pago procesado correctamente");
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("error con mensaje - debe retornar success false con data null")
    void error_WithMessage_ReturnsSuccessFalseWithNullData() {
        // When
        ApiResponse<Object> response = ApiResponse.error("Error al procesar pago");

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Error al procesar pago");
        assertThat(response.getData()).isNull();
    }
}
