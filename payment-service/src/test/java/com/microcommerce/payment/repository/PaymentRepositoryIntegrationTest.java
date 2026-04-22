package com.microcommerce.payment.repository;

import com.microcommerce.payment.entity.Payment;
import com.microcommerce.payment.entity.PaymentMethod;
import com.microcommerce.payment.entity.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for PaymentRepository using TestContainers with PostgreSQL
 * Tests de integracion para PaymentRepository usando TestContainers con PostgreSQL
 */
@DataJpaTest
@Testcontainers
@ActiveProfiles("test-tc")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("PaymentRepository Integration Tests")
class PaymentRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("payment_test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment completedPayment;
    private Payment pendingPayment;
    private Payment failedPayment;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();

        completedPayment = paymentRepository.save(Payment.builder()
                .orderId(100L)
                .userId(200L)
                .amount(new BigDecimal("99.99"))
                .currency("USD")
                .status(PaymentStatus.COMPLETED)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .stripePaymentIntentId("pi_test_completed_001")
                .stripeChargeId("ch_test_charge_001")
                .description("Completed payment")
                .build());

        pendingPayment = paymentRepository.save(Payment.builder()
                .orderId(101L)
                .userId(200L)
                .amount(new BigDecimal("49.50"))
                .currency("USD")
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.DEBIT_CARD)
                .description("Pending payment")
                .build());

        failedPayment = paymentRepository.save(Payment.builder()
                .orderId(102L)
                .userId(300L)
                .amount(new BigDecimal("15.00"))
                .currency("EUR")
                .status(PaymentStatus.FAILED)
                .paymentMethod(PaymentMethod.DIGITAL_WALLET)
                .failureReason("Tarjeta rechazada")
                .description("Failed payment")
                .build());
    }

    @Test
    @DisplayName("findByOrderId - debe retornar pagos de la orden")
    void findByOrderId_ReturnsPaymentsForOrder() {
        // When
        List<Payment> result = paymentRepository.findByOrderId(100L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo(100L);
        assertThat(result.get(0).getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    @DisplayName("findByOrderId - orden sin pagos - debe retornar lista vacia")
    void findByOrderId_NoPayments_ReturnsEmptyList() {
        // When
        List<Payment> result = paymentRepository.findByOrderId(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUserId - debe retornar pagos del usuario")
    void findByUserId_ReturnsPaymentsForUser() {
        // When
        List<Payment> result = paymentRepository.findByUserId(200L);

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findByStripePaymentIntentId - ID existente - debe retornar pago")
    void findByStripePaymentIntentId_ExistingId_ReturnsPayment() {
        // When
        Optional<Payment> result = paymentRepository
                .findByStripePaymentIntentId("pi_test_completed_001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStripePaymentIntentId()).isEqualTo("pi_test_completed_001");
    }

    @Test
    @DisplayName("findByStripePaymentIntentId - ID no existente - debe retornar empty")
    void findByStripePaymentIntentId_NonExistingId_ReturnsEmpty() {
        // When
        Optional<Payment> result = paymentRepository
                .findByStripePaymentIntentId("pi_nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByStatus - debe retornar pagos con el estado dado")
    void findByStatus_ReturnsPaymentsWithGivenStatus() {
        // When
        List<Payment> result = paymentRepository.findByStatus(PaymentStatus.COMPLETED);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    @DisplayName("findByUserIdAndStatus - debe retornar pagos filtrados por usuario y estado")
    void findByUserIdAndStatus_ReturnsFilteredPayments() {
        // When
        List<Payment> result = paymentRepository
                .findByUserIdAndStatus(200L, PaymentStatus.PENDING);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(200L);
        assertThat(result.get(0).getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("countByStatus - debe retornar cantidad correcta")
    void countByStatus_ReturnsCorrectCount() {
        // When
        long completedCount = paymentRepository.countByStatus(PaymentStatus.COMPLETED);
        long pendingCount = paymentRepository.countByStatus(PaymentStatus.PENDING);
        long refundedCount = paymentRepository.countByStatus(PaymentStatus.REFUNDED);

        // Then
        assertThat(completedCount).isEqualTo(1);
        assertThat(pendingCount).isEqualTo(1);
        assertThat(refundedCount).isZero();
    }

    @Test
    @DisplayName("existsByOrderIdAndStatusIn - orden con pago activo - debe retornar true")
    void existsByOrderIdAndStatusIn_ActivePayment_ReturnsTrue() {
        // When
        boolean exists = paymentRepository.existsByOrderIdAndStatusIn(
                100L, List.of(PaymentStatus.COMPLETED, PaymentStatus.PROCESSING));

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByOrderIdAndStatusIn - orden sin pago activo - debe retornar false")
    void existsByOrderIdAndStatusIn_NoActivePayment_ReturnsFalse() {
        // When
        boolean exists = paymentRepository.existsByOrderIdAndStatusIn(
                102L, List.of(PaymentStatus.COMPLETED, PaymentStatus.PROCESSING));

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("save - nuevo pago - debe persistir correctamente con timestamps")
    void save_NewPayment_PersistsWithTimestamps() {
        // Given
        Payment newPayment = Payment.builder()
                .orderId(500L)
                .userId(600L)
                .amount(new BigDecimal("200.00"))
                .currency("USD")
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .description("New test payment")
                .build();

        // When
        Payment saved = paymentRepository.save(newPayment);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getOrderId()).isEqualTo(500L);
        assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("findByCreatedAtBetween - debe retornar pagos en rango de fechas")
    void findByCreatedAtBetween_ReturnsPaymentsInDateRange() {
        // When - all test payments were just created, so they should be in the current time range
        List<Payment> result = paymentRepository.findByCreatedAtBetween(
                java.time.LocalDateTime.now().minusMinutes(5),
                java.time.LocalDateTime.now().plusMinutes(5));

        // Then
        assertThat(result).hasSize(3);
    }
}
