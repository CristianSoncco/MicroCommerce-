package com.microcommerce.payment.mapper;

import com.microcommerce.payment.dto.request.PaymentRequest;
import com.microcommerce.payment.dto.response.PaymentResponse;
import com.microcommerce.payment.entity.Payment;
import com.microcommerce.payment.entity.PaymentMethod;
import com.microcommerce.payment.entity.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for PaymentMapper
 * Tests unitarios para PaymentMapper
 */
@DisplayName("PaymentMapper Tests")
class PaymentMapperTest {

    private PaymentMapper paymentMapper;

    @BeforeEach
    void setUp() {
        paymentMapper = new PaymentMapper();
    }

    // ==================== toEntity Tests ====================

    @Test
    @DisplayName("toEntity - request valido - debe retornar Payment entity completa")
    void toEntity_ValidRequest_ReturnsPaymentEntity() {
        // Given
        PaymentRequest request = PaymentRequest.builder()
                .orderId(100L)
                .userId(200L)
                .amount(new BigDecimal("99.99"))
                .currency("USD")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .description("Test payment")
                .paymentToken("pm_test_visa_001")
                .build();

        // When
        Payment result = paymentMapper.toEntity(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(100L);
        assertThat(result.getUserId()).isEqualTo(200L);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(result.getCurrency()).isEqualTo("USD");
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(result.getDescription()).isEqualTo("Test payment");
    }

    @Test
    @DisplayName("toEntity - moneda nula - debe usar USD por defecto")
    void toEntity_NullCurrency_UsesDefaultUsd() {
        // Given
        PaymentRequest request = PaymentRequest.builder()
                .orderId(100L)
                .userId(200L)
                .amount(new BigDecimal("50.00"))
                .currency(null)
                .paymentMethod(PaymentMethod.DEBIT_CARD)
                .build();

        // When
        Payment result = paymentMapper.toEntity(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCurrency()).isEqualTo("USD");
    }

    @Test
    @DisplayName("toEntity - request nulo - debe retornar null")
    void toEntity_NullRequest_ReturnsNull() {
        // When
        Payment result = paymentMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    // ==================== toResponse Tests ====================

    @Test
    @DisplayName("toResponse - payment valido - debe retornar PaymentResponse completo")
    void toResponse_ValidPayment_ReturnsPaymentResponse() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Payment payment = Payment.builder()
                .id(1L)
                .orderId(100L)
                .userId(200L)
                .amount(new BigDecimal("99.99"))
                .currency("USD")
                .status(PaymentStatus.COMPLETED)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .stripePaymentIntentId("pi_test_123")
                .description("Test payment")
                .failureReason(null)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        PaymentResponse result = paymentMapper.toResponse(payment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getOrderId()).isEqualTo(100L);
        assertThat(result.getUserId()).isEqualTo(200L);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(result.getCurrency()).isEqualTo("USD");
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(result.getStripePaymentIntentId()).isEqualTo("pi_test_123");
        assertThat(result.getDescription()).isEqualTo("Test payment");
        assertThat(result.getFailureReason()).isNull();
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("toResponse - payment con failure reason - debe incluir failure reason")
    void toResponse_PaymentWithFailureReason_IncludesFailureReason() {
        // Given
        Payment payment = Payment.builder()
                .id(1L)
                .orderId(100L)
                .userId(200L)
                .amount(new BigDecimal("99.99"))
                .currency("USD")
                .status(PaymentStatus.FAILED)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .failureReason("Tarjeta rechazada")
                .build();

        // When
        PaymentResponse result = paymentMapper.toResponse(payment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(result.getFailureReason()).isEqualTo("Tarjeta rechazada");
    }

    @Test
    @DisplayName("toResponse - payment nulo - debe retornar null")
    void toResponse_NullPayment_ReturnsNull() {
        // When
        PaymentResponse result = paymentMapper.toResponse(null);

        // Then
        assertThat(result).isNull();
    }
}
