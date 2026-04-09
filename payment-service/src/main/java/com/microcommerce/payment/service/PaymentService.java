package com.microcommerce.payment.service;

import com.microcommerce.payment.dto.request.PaymentRequest;
import com.microcommerce.payment.dto.request.RefundRequest;
import com.microcommerce.payment.dto.response.PaymentResponse;
import com.microcommerce.payment.entity.PaymentStatus;

import java.util.List;

/**
 * Service interface for Payment operations
 * Interfaz de servicio para operaciones de Payment
 */
public interface PaymentService {

    /**
     * Process a new payment through Stripe
     * Procesar un nuevo pago a traves de Stripe
     *
     * @param request the payment request details
     * @return the payment response with Stripe confirmation
     */
    PaymentResponse processPayment(PaymentRequest request);

    /**
     * Get payment by ID
     * Obtener pago por ID
     *
     * @param id the payment ID
     * @return the payment response
     */
    PaymentResponse getPaymentById(Long id);

    /**
     * Get payment by Stripe PaymentIntent ID
     * Obtener pago por ID de PaymentIntent de Stripe
     *
     * @param stripePaymentIntentId the Stripe PaymentIntent ID
     * @return the payment response
     */
    PaymentResponse getPaymentByStripeIntentId(String stripePaymentIntentId);

    /**
     * Get all payments for an order
     * Obtener todos los pagos de una orden
     *
     * @param orderId the order ID
     * @return list of payment responses
     */
    List<PaymentResponse> getPaymentsByOrderId(Long orderId);

    /**
     * Get all payments for a user
     * Obtener todos los pagos de un usuario
     *
     * @param userId the user ID
     * @return list of payment responses
     */
    List<PaymentResponse> getPaymentsByUserId(Long userId);

    /**
     * Get payments by user ID and status
     * Obtener pagos por ID de usuario y estado
     *
     * @param userId the user ID
     * @param status the payment status
     * @return list of payment responses
     */
    List<PaymentResponse> getPaymentsByUserIdAndStatus(Long userId, PaymentStatus status);

    /**
     * Get payments by status
     * Obtener pagos por estado
     *
     * @param status the payment status
     * @return list of payment responses
     */
    List<PaymentResponse> getPaymentsByStatus(PaymentStatus status);

    /**
     * Process a refund for an existing payment
     * Procesar un reembolso para un pago existente
     *
     * @param paymentId the payment ID to refund
     * @param request the refund request details
     * @return the updated payment response
     */
    PaymentResponse refundPayment(Long paymentId, RefundRequest request);

    /**
     * Cancel a pending payment
     * Cancelar un pago pendiente
     *
     * @param paymentId the payment ID to cancel
     * @return the updated payment response
     */
    PaymentResponse cancelPayment(Long paymentId);
}
