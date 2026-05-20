package com.microcommerce.payment.service;

import com.microcommerce.payment.client.StripeClient;
import com.microcommerce.payment.dto.request.PaymentRequest;
import com.microcommerce.payment.dto.request.RefundRequest;
import com.microcommerce.payment.dto.request.StripePaymentIntentRequest;
import com.microcommerce.payment.dto.response.PaymentResponse;
import com.microcommerce.payment.dto.response.StripePaymentIntentResponse;
import com.microcommerce.payment.dto.response.StripeRefundResponse;
import com.microcommerce.payment.entity.Payment;
import com.microcommerce.payment.entity.PaymentStatus;
import com.microcommerce.payment.event.PaymentEventPublisher;
import com.microcommerce.payment.exception.InvalidPaymentStateException;
import com.microcommerce.payment.exception.PaymentAlreadyProcessedException;
import com.microcommerce.payment.exception.PaymentNotFoundException;
import com.microcommerce.payment.mapper.PaymentMapper;
import com.microcommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementation of PaymentService with Stripe integration
 * Implementacion de PaymentService con integracion a Stripe
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final StripeClient stripeClient;
    private final PaymentEventPublisher paymentEventPublisher;

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Procesando pago para orden: {}, usuario: {}, monto: {} {}",
                request.getOrderId(), request.getUserId(), request.getAmount(), request.getCurrency());

        // Verify no completed or processing payment exists for this order
        boolean existsActivePayment = paymentRepository.existsByOrderIdAndStatusIn(
                request.getOrderId(),
                List.of(PaymentStatus.COMPLETED, PaymentStatus.PROCESSING));

        if (existsActivePayment) {
            throw new PaymentAlreadyProcessedException(request.getOrderId());
        }

        // Create payment entity in PENDING status
        Payment payment = paymentMapper.toEntity(request);
        payment.setStatus(PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);
        log.info("Pago creado con ID: {} en estado PENDING", payment.getId());

        // Build Stripe request (amount in cents)
        long amountInCents = request.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
        StripePaymentIntentRequest stripeRequest = StripePaymentIntentRequest.builder()
                .amount(amountInCents)
                .currency(request.getCurrency() != null ? request.getCurrency().toLowerCase() : "usd")
                .paymentMethodToken(request.getPaymentToken())
                .description(request.getDescription())
                .confirm(true)
                .build();

        // Update to PROCESSING before calling Stripe
        payment.setStatus(PaymentStatus.PROCESSING);
        payment = paymentRepository.save(payment);

        try {
            // Call Stripe to create and confirm PaymentIntent
            StripePaymentIntentResponse stripeResponse = stripeClient.createPaymentIntent(stripeRequest);

            // Update payment with Stripe response
            payment.setStripePaymentIntentId(stripeResponse.getId());
            payment.setStripeChargeId(stripeResponse.getLatestCharge());

            if (stripeResponse.isSucceeded()) {
                payment.setStatus(PaymentStatus.COMPLETED);
                log.info("Pago completado exitosamente. PaymentIntent: {}", stripeResponse.getId());
            } else if (stripeResponse.requiresAction()) {
                payment.setStatus(PaymentStatus.PROCESSING);
                log.info("Pago requiere accion adicional. PaymentIntent: {}", stripeResponse.getId());
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Estado inesperado de Stripe: " + stripeResponse.getStatus());
                log.warn("Pago con estado inesperado de Stripe: {}", stripeResponse.getStatus());
            }

        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            log.error("Error al procesar pago con Stripe: {}", e.getMessage());
        }

        payment = paymentRepository.save(payment);

        // Publish event based on final payment status
        // Publicar evento segun el estado final del pago
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            paymentEventPublisher.publishPaymentCompleted(payment);
        } else if (payment.getStatus() == PaymentStatus.FAILED) {
            paymentEventPublisher.publishPaymentFailed(payment);
        }

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        log.debug("Buscando pago con ID: {}", id);
        Payment payment = findPaymentByIdOrThrow(id);
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByStripeIntentId(String stripePaymentIntentId) {
        log.debug("Buscando pago con Stripe PaymentIntent ID: {}", stripePaymentIntentId);
        Payment payment = paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Pago no encontrado con Stripe PaymentIntent ID: " + stripePaymentIntentId));
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        log.debug("Buscando todos los pagos");
        return paymentRepository.findAll().stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrderId(String orderId) {
        log.debug("Buscando pagos para orden: {}", orderId);
        return paymentRepository.findByOrderId(orderId).stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        log.debug("Buscando pagos para usuario: {}", userId);
        return paymentRepository.findByUserId(userId).stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUserIdAndStatus(Long userId, PaymentStatus status) {
        log.debug("Buscando pagos para usuario: {} con estado: {}", userId, status);
        return paymentRepository.findByUserIdAndStatus(userId, status).stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        log.debug("Buscando pagos con estado: {}", status);
        return paymentRepository.findByStatus(status).stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    public PaymentResponse refundPayment(Long paymentId, RefundRequest request) {
        log.info("Procesando reembolso para pago ID: {}", paymentId);

        Payment payment = findPaymentByIdOrThrow(paymentId);

        // Only completed payments can be refunded
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new InvalidPaymentStateException(
                    "Solo se pueden reembolsar pagos completados. Estado actual: " + payment.getStatus());
        }

        // Validate refund amount does not exceed original
        if (request.getAmount() != null && request.getAmount().compareTo(payment.getAmount()) > 0) {
            throw new InvalidPaymentStateException(
                    "El monto de reembolso no puede exceder el monto original del pago: " + payment.getAmount());
        }

        // Call Stripe to create refund
        StripeRefundResponse stripeResponse = stripeClient.createRefund(
                payment.getStripePaymentIntentId(),
                request.getAmount(),
                request.getReason());

        if (stripeResponse.isSucceeded()) {
            payment.setStatus(PaymentStatus.REFUNDED);
            log.info("Reembolso procesado exitosamente para pago ID: {}", paymentId);
        } else {
            payment.setFailureReason("Reembolso en estado: " + stripeResponse.getStatus());
            log.warn("Reembolso no completado inmediatamente para pago ID: {}. Estado: {}",
                    paymentId, stripeResponse.getStatus());
        }

        payment = paymentRepository.save(payment);

        // Publish PAYMENT_REFUNDED event when refund completes successfully
        // Publicar evento PAYMENT_REFUNDED cuando el reembolso se completa exitosamente
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            paymentEventPublisher.publishPaymentRefunded(payment);
        }

        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse cancelPayment(Long paymentId) {
        log.info("Cancelando pago ID: {}", paymentId);

        Payment payment = findPaymentByIdOrThrow(paymentId);

        // Only pending payments can be cancelled
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException(
                    "Solo se pueden cancelar pagos pendientes. Estado actual: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment = paymentRepository.save(payment);

        log.info("Pago cancelado exitosamente. ID: {}", paymentId);
        return paymentMapper.toResponse(payment);
    }

    /**
     * Find payment by ID or throw PaymentNotFoundException
     * Buscar pago por ID o lanzar PaymentNotFoundException
     */
    private Payment findPaymentByIdOrThrow(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }
}
