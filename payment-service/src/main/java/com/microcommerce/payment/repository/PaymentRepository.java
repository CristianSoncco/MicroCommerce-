package com.microcommerce.payment.repository;

import com.microcommerce.payment.entity.Payment;
import com.microcommerce.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Payment entity
 * Interfaz de repositorio para la entidad Payment
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payments by order ID
     * Buscar pagos por ID de orden
     */
    List<Payment> findByOrderId(Long orderId);

    /**
     * Find payments by user ID
     * Buscar pagos por ID de usuario
     */
    List<Payment> findByUserId(Long userId);

    /**
     * Find payment by Stripe payment intent ID
     * Buscar pago por ID de intent de pago de Stripe
     */
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

    /**
     * Find payments by status
     * Buscar pagos por estado
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Find payments by user ID and status
     * Buscar pagos por ID de usuario y estado
     */
    List<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status);

    /**
     * Find payments created between dates
     * Buscar pagos creados entre fechas
     */
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count payments by status
     * Contar pagos por estado
     */
    long countByStatus(PaymentStatus status);

    /**
     * Check if a payment exists for a given order
     * Verificar si existe un pago para una orden dada
     */
    boolean existsByOrderIdAndStatusIn(Long orderId, List<PaymentStatus> statuses);
}
