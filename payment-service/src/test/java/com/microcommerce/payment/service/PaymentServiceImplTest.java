package com.microcommerce.payment.service;

import com.microcommerce.payment.client.StripeClient;
import com.microcommerce.payment.dto.request.PaymentRequest;
import com.microcommerce.payment.dto.request.RefundRequest;
import com.microcommerce.payment.dto.response.PaymentResponse;
import com.microcommerce.payment.dto.response.StripePaymentIntentResponse;
import com.microcommerce.payment.dto.response.StripeRefundResponse;
import com.microcommerce.payment.entity.Payment;
import com.microcommerce.payment.entity.PaymentMethod;
import com.microcommerce.payment.entity.PaymentStatus;
import com.microcommerce.payment.event.PaymentEventPublisher;
import com.microcommerce.payment.exception.InvalidPaymentStateException;
import com.microcommerce.payment.exception.PaymentAlreadyProcessedException;
import com.microcommerce.payment.exception.PaymentGatewayException;
import com.microcommerce.payment.exception.PaymentNotFoundException;
import com.microcommerce.payment.mapper.PaymentMapper;
import com.microcommerce.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentServiceImpl
 * Tests unitarios para PaymentServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PaymentServiceImpl Tests")
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private StripeClient stripeClient;

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment payment;
    private Payment completedPayment;
    private Payment pendingPayment;
    private PaymentRequest paymentRequest;
    private RefundRequest refundRequest;
    private PaymentResponse paymentResponse;
    private StripePaymentIntentResponse stripeSuccessResponse;
    private StripeRefundResponse stripeRefundSuccessResponse;

    @BeforeEach
    void setUp() {
        // Setup pending payment
        pendingPayment = Payment.builder()
                .id(1L)
                .orderId(100L)
                .userId(200L)
                .amount(new BigDecimal("99.99"))
                .currency("USD")
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .description("Order #100 payment")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Setup completed payment
        completedPayment = Payment.builder()
                .id(2L)
                .orderId(101L)
                .userId(200L)
                .amount(new BigDecimal("249.50"))
                .currency("USD")
                .status(PaymentStatus.COMPLETED)
                .paymentMethod(PaymentMethod.DEBIT_CARD)
                .stripePaymentIntentId("pi_test_completed_001")
                .stripeChargeId("ch_test_charge_001")
                .description("Order #101 payment")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Default payment used in process tests
        payment = Payment.builder()
                .id(1L)
                .orderId(100L)
                .userId(200L)
                .amount(new BigDecimal("99.99"))
                .currency("USD")
                .status(PaymentStatus.PENDING)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .description("Order #100 payment")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Setup PaymentRequest
        paymentRequest = PaymentRequest.builder()
                .orderId(100L)
                .userId(200L)
                .amount(new BigDecimal("99.99"))
                .currency("USD")
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .description("Order #100 payment")
                .paymentToken("pm_test_visa_001")
                .build();

        // Setup RefundRequest
        refundRequest = RefundRequest.builder()
                .amount(new BigDecimal("50.00"))
                .reason("Customer requested refund")
                .build();

        // Setup PaymentResponse
        paymentResponse = PaymentResponse.builder()
                .id(1L)
                .orderId(100L)
                .userId(200L)
                .amount(new BigDecimal("99.99"))
                .currency("USD")
                .status(PaymentStatus.COMPLETED)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .stripePaymentIntentId("pi_test_123456789")
                .description("Order #100 payment")
                .build();

        // Setup Stripe success response
        stripeSuccessResponse = StripePaymentIntentResponse.builder()
                .id("pi_test_123456789")
                .object("payment_intent")
                .amount(9999L)
                .currency("usd")
                .status("succeeded")
                .clientSecret("pi_test_123456789_secret_abc")
                .paymentMethod("pm_test_visa_001")
                .latestCharge("ch_test_charge_001")
                .description("Order #100 payment")
                .created(1700000000L)
                .build();

        // Setup Stripe refund success response
        stripeRefundSuccessResponse = StripeRefundResponse.builder()
                .id("re_test_refund_001")
                .object("refund")
                .amount(5000L)
                .currency("usd")
                .paymentIntent("pi_test_completed_001")
                .status("succeeded")
                .reason("requested_by_customer")
                .created(1700001000L)
                .build();

        // Common mock setups
        when(paymentMapper.toEntity(any(PaymentRequest.class))).thenReturn(payment);
        when(paymentMapper.toResponse(any(Payment.class))).thenReturn(paymentResponse);
    }

    // ==================== processPayment Tests ====================

    @Test
    @DisplayName("processPayment - pago exitoso con Stripe - debe retornar PaymentResponse completado")
    void processPayment_SuccessfulStripePayment_ReturnsCompletedPaymentResponse() {
        // Given
        when(paymentRepository.existsByOrderIdAndStatusIn(eq(100L), anyList())).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(stripeClient.createPaymentIntent(any())).thenReturn(stripeSuccessResponse);

        // When
        PaymentResponse result = paymentService.processPayment(paymentRequest);

        // Then
        assertThat(result).isNotNull();
        verify(paymentRepository).existsByOrderIdAndStatusIn(eq(100L), anyList());
        verify(paymentMapper).toEntity(paymentRequest);
        verify(stripeClient).createPaymentIntent(any());
        verify(paymentRepository, times(3)).save(any(Payment.class));
    }

    @Test
    @DisplayName("processPayment - orden con pago existente - debe lanzar PaymentAlreadyProcessedException")
    void processPayment_ExistingActivePayment_ThrowsPaymentAlreadyProcessedException() {
        // Given
        when(paymentRepository.existsByOrderIdAndStatusIn(eq(100L), anyList())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(PaymentAlreadyProcessedException.class);
        verify(paymentRepository, never()).save(any());
        verify(stripeClient, never()).createPaymentIntent(any());
    }

    @Test
    @DisplayName("processPayment - Stripe requiere accion adicional - debe quedar en PROCESSING")
    void processPayment_StripeRequiresAction_StatusProcessing() {
        // Given
        StripePaymentIntentResponse requiresActionResponse = StripePaymentIntentResponse.builder()
                .id("pi_test_requires_action")
                .status("requires_action")
                .latestCharge(null)
                .build();

        when(paymentRepository.existsByOrderIdAndStatusIn(eq(100L), anyList())).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(stripeClient.createPaymentIntent(any())).thenReturn(requiresActionResponse);

        // When
        paymentService.processPayment(paymentRequest);

        // Then
        verify(stripeClient).createPaymentIntent(any());
        verify(paymentRepository, times(3)).save(any(Payment.class));
    }

    @Test
    @DisplayName("processPayment - Stripe retorna estado inesperado - debe quedar en FAILED")
    void processPayment_StripeUnexpectedStatus_StatusFailed() {
        // Given
        StripePaymentIntentResponse unexpectedResponse = StripePaymentIntentResponse.builder()
                .id("pi_test_unexpected")
                .status("canceled")
                .latestCharge(null)
                .build();

        when(paymentRepository.existsByOrderIdAndStatusIn(eq(100L), anyList())).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(stripeClient.createPaymentIntent(any())).thenReturn(unexpectedResponse);

        // When
        paymentService.processPayment(paymentRequest);

        // Then
        verify(paymentRepository, times(3)).save(any(Payment.class));
    }

    @Test
    @DisplayName("processPayment - error de Stripe - debe marcar pago como FAILED")
    void processPayment_StripeException_PaymentMarkedAsFailed() {
        // Given
        when(paymentRepository.existsByOrderIdAndStatusIn(eq(100L), anyList())).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(stripeClient.createPaymentIntent(any()))
                .thenThrow(new PaymentGatewayException("Error de conexion con Stripe"));

        // When
        PaymentResponse result = paymentService.processPayment(paymentRequest);

        // Then
        assertThat(result).isNotNull();
        verify(paymentRepository, times(3)).save(any(Payment.class));
    }

    @Test
    @DisplayName("processPayment - moneda nula - debe usar USD por defecto")
    void processPayment_NullCurrency_UsesDefaultUsd() {
        // Given
        PaymentRequest requestNullCurrency = PaymentRequest.builder()
                .orderId(100L)
                .userId(200L)
                .amount(new BigDecimal("99.99"))
                .currency(null)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentToken("pm_test_visa_001")
                .build();

        when(paymentRepository.existsByOrderIdAndStatusIn(eq(100L), anyList())).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(stripeClient.createPaymentIntent(any())).thenReturn(stripeSuccessResponse);

        // When
        paymentService.processPayment(requestNullCurrency);

        // Then
        verify(stripeClient).createPaymentIntent(argThat(req ->
                "usd".equals(req.getCurrency())));
    }

    // ==================== getPaymentById Tests ====================

    @Test
    @DisplayName("getPaymentById - ID existente - debe retornar PaymentResponse")
    void getPaymentById_ExistingId_ReturnsPaymentResponse() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // When
        PaymentResponse result = paymentService.getPaymentById(1L);

        // Then
        assertThat(result).isNotNull();
        verify(paymentRepository).findById(1L);
        verify(paymentMapper).toResponse(payment);
    }

    @Test
    @DisplayName("getPaymentById - ID no existente - debe lanzar PaymentNotFoundException")
    void getPaymentById_NonExistingId_ThrowsPaymentNotFoundException() {
        // Given
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentService.getPaymentById(999L))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    // ==================== getPaymentByStripeIntentId Tests ====================

    @Test
    @DisplayName("getPaymentByStripeIntentId - ID existente - debe retornar PaymentResponse")
    void getPaymentByStripeIntentId_ExistingId_ReturnsPaymentResponse() {
        // Given
        when(paymentRepository.findByStripePaymentIntentId("pi_test_123456789"))
                .thenReturn(Optional.of(payment));

        // When
        PaymentResponse result = paymentService.getPaymentByStripeIntentId("pi_test_123456789");

        // Then
        assertThat(result).isNotNull();
        verify(paymentRepository).findByStripePaymentIntentId("pi_test_123456789");
    }

    @Test
    @DisplayName("getPaymentByStripeIntentId - ID no existente - debe lanzar PaymentNotFoundException")
    void getPaymentByStripeIntentId_NonExistingId_ThrowsPaymentNotFoundException() {
        // Given
        when(paymentRepository.findByStripePaymentIntentId("pi_nonexistent"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentService.getPaymentByStripeIntentId("pi_nonexistent"))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("pi_nonexistent");
    }

    // ==================== getPaymentsByOrderId Tests ====================

    @Test
    @DisplayName("getPaymentsByOrderId - orden con pagos - debe retornar lista de PaymentResponse")
    void getPaymentsByOrderId_OrderWithPayments_ReturnsPaymentResponseList() {
        // Given
        when(paymentRepository.findByOrderId(100L)).thenReturn(List.of(payment));

        // When
        List<PaymentResponse> result = paymentService.getPaymentsByOrderId(100L);

        // Then
        assertThat(result).hasSize(1);
        verify(paymentRepository).findByOrderId(100L);
    }

    @Test
    @DisplayName("getPaymentsByOrderId - orden sin pagos - debe retornar lista vacia")
    void getPaymentsByOrderId_OrderWithNoPayments_ReturnsEmptyList() {
        // Given
        when(paymentRepository.findByOrderId(999L)).thenReturn(Collections.emptyList());

        // When
        List<PaymentResponse> result = paymentService.getPaymentsByOrderId(999L);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== getPaymentsByUserId Tests ====================

    @Test
    @DisplayName("getPaymentsByUserId - usuario con pagos - debe retornar lista")
    void getPaymentsByUserId_UserWithPayments_ReturnsList() {
        // Given
        when(paymentRepository.findByUserId(200L)).thenReturn(List.of(payment, completedPayment));

        // When
        List<PaymentResponse> result = paymentService.getPaymentsByUserId(200L);

        // Then
        assertThat(result).hasSize(2);
        verify(paymentRepository).findByUserId(200L);
    }

    @Test
    @DisplayName("getPaymentsByUserId - usuario sin pagos - debe retornar lista vacia")
    void getPaymentsByUserId_UserWithNoPayments_ReturnsEmptyList() {
        // Given
        when(paymentRepository.findByUserId(999L)).thenReturn(Collections.emptyList());

        // When
        List<PaymentResponse> result = paymentService.getPaymentsByUserId(999L);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== getPaymentsByUserIdAndStatus Tests ====================

    @Test
    @DisplayName("getPaymentsByUserIdAndStatus - usuario y estado validos - debe retornar lista filtrada")
    void getPaymentsByUserIdAndStatus_ValidUserAndStatus_ReturnsFilteredList() {
        // Given
        when(paymentRepository.findByUserIdAndStatus(200L, PaymentStatus.COMPLETED))
                .thenReturn(List.of(completedPayment));

        // When
        List<PaymentResponse> result = paymentService.getPaymentsByUserIdAndStatus(200L, PaymentStatus.COMPLETED);

        // Then
        assertThat(result).hasSize(1);
        verify(paymentRepository).findByUserIdAndStatus(200L, PaymentStatus.COMPLETED);
    }

    // ==================== getPaymentsByStatus Tests ====================

    @Test
    @DisplayName("getPaymentsByStatus - estado con pagos - debe retornar lista")
    void getPaymentsByStatus_StatusWithPayments_ReturnsList() {
        // Given
        when(paymentRepository.findByStatus(PaymentStatus.PENDING))
                .thenReturn(List.of(pendingPayment));

        // When
        List<PaymentResponse> result = paymentService.getPaymentsByStatus(PaymentStatus.PENDING);

        // Then
        assertThat(result).hasSize(1);
        verify(paymentRepository).findByStatus(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("getPaymentsByStatus - estado sin pagos - debe retornar lista vacia")
    void getPaymentsByStatus_StatusWithNoPayments_ReturnsEmptyList() {
        // Given
        when(paymentRepository.findByStatus(PaymentStatus.REFUNDED))
                .thenReturn(Collections.emptyList());

        // When
        List<PaymentResponse> result = paymentService.getPaymentsByStatus(PaymentStatus.REFUNDED);

        // Then
        assertThat(result).isEmpty();
    }

    // ==================== refundPayment Tests ====================

    @Test
    @DisplayName("refundPayment - pago completado con reembolso valido - debe retornar PaymentResponse reembolsado")
    void refundPayment_CompletedPaymentValidRefund_ReturnsRefundedPaymentResponse() {
        // Given
        when(paymentRepository.findById(2L)).thenReturn(Optional.of(completedPayment));
        when(stripeClient.createRefund("pi_test_completed_001", new BigDecimal("50.00"), "Customer requested refund"))
                .thenReturn(stripeRefundSuccessResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(completedPayment);

        // When
        PaymentResponse result = paymentService.refundPayment(2L, refundRequest);

        // Then
        assertThat(result).isNotNull();
        verify(stripeClient).createRefund("pi_test_completed_001", new BigDecimal("50.00"), "Customer requested refund");
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("refundPayment - pago no completado - debe lanzar InvalidPaymentStateException")
    void refundPayment_NonCompletedPayment_ThrowsInvalidPaymentStateException() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(pendingPayment));

        // When & Then
        assertThatThrownBy(() -> paymentService.refundPayment(1L, refundRequest))
                .isInstanceOf(InvalidPaymentStateException.class)
                .hasMessageContaining("Solo se pueden reembolsar pagos completados");
        verify(stripeClient, never()).createRefund(any(), any(), any());
    }

    @Test
    @DisplayName("refundPayment - monto mayor al pago original - debe lanzar InvalidPaymentStateException")
    void refundPayment_AmountExceedsOriginal_ThrowsInvalidPaymentStateException() {
        // Given
        RefundRequest excessiveRefund = RefundRequest.builder()
                .amount(new BigDecimal("500.00"))
                .reason("Excessive refund attempt")
                .build();
        when(paymentRepository.findById(2L)).thenReturn(Optional.of(completedPayment));

        // When & Then
        assertThatThrownBy(() -> paymentService.refundPayment(2L, excessiveRefund))
                .isInstanceOf(InvalidPaymentStateException.class)
                .hasMessageContaining("El monto de reembolso no puede exceder");
        verify(stripeClient, never()).createRefund(any(), any(), any());
    }

    @Test
    @DisplayName("refundPayment - pago no encontrado - debe lanzar PaymentNotFoundException")
    void refundPayment_PaymentNotFound_ThrowsPaymentNotFoundException() {
        // Given
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentService.refundPayment(999L, refundRequest))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    @DisplayName("refundPayment - Stripe retorna estado no exitoso - debe registrar warning")
    void refundPayment_StripeNonSucceededStatus_SetsFailureReason() {
        // Given
        StripeRefundResponse pendingRefundResponse = StripeRefundResponse.builder()
                .id("re_test_pending")
                .status("pending")
                .build();

        when(paymentRepository.findById(2L)).thenReturn(Optional.of(completedPayment));
        when(stripeClient.createRefund(any(), any(), any())).thenReturn(pendingRefundResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(completedPayment);

        // When
        paymentService.refundPayment(2L, refundRequest);

        // Then
        verify(paymentRepository).save(argThat(p ->
                p.getFailureReason() != null && p.getFailureReason().contains("pending")));
    }

    @Test
    @DisplayName("refundPayment - monto nulo (reembolso completo) - no debe lanzar excepcion de monto")
    void refundPayment_NullAmount_FullRefund_NoException() {
        // Given
        RefundRequest fullRefund = RefundRequest.builder()
                .amount(null)
                .reason("Full refund")
                .build();

        when(paymentRepository.findById(2L)).thenReturn(Optional.of(completedPayment));
        when(stripeClient.createRefund("pi_test_completed_001", null, "Full refund"))
                .thenReturn(stripeRefundSuccessResponse);
        when(paymentRepository.save(any(Payment.class))).thenReturn(completedPayment);

        // When
        PaymentResponse result = paymentService.refundPayment(2L, fullRefund);

        // Then
        assertThat(result).isNotNull();
        verify(stripeClient).createRefund("pi_test_completed_001", null, "Full refund");
    }

    // ==================== cancelPayment Tests ====================

    @Test
    @DisplayName("cancelPayment - pago pendiente - debe retornar PaymentResponse cancelado")
    void cancelPayment_PendingPayment_ReturnsCancelledPaymentResponse() {
        // Given
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(pendingPayment);

        // When
        PaymentResponse result = paymentService.cancelPayment(1L);

        // Then
        assertThat(result).isNotNull();
        verify(paymentRepository).save(argThat(p -> p.getStatus() == PaymentStatus.CANCELLED));
    }

    @Test
    @DisplayName("cancelPayment - pago no pendiente - debe lanzar InvalidPaymentStateException")
    void cancelPayment_NonPendingPayment_ThrowsInvalidPaymentStateException() {
        // Given
        when(paymentRepository.findById(2L)).thenReturn(Optional.of(completedPayment));

        // When & Then
        assertThatThrownBy(() -> paymentService.cancelPayment(2L))
                .isInstanceOf(InvalidPaymentStateException.class)
                .hasMessageContaining("Solo se pueden cancelar pagos pendientes");
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelPayment - pago no encontrado - debe lanzar PaymentNotFoundException")
    void cancelPayment_PaymentNotFound_ThrowsPaymentNotFoundException() {
        // Given
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentService.cancelPayment(999L))
                .isInstanceOf(PaymentNotFoundException.class);
    }
}
