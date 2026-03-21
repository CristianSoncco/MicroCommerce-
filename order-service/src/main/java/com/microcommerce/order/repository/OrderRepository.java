package com.microcommerce.order.repository;

import com.microcommerce.order.entity.Order;
import com.microcommerce.order.entity.Order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Repository
 * Repositorio de Pedidos
 * 
 * Interface para acceso a datos de pedidos usando Spring Data JPA.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find all orders by user ID
     * Buscar todos los pedidos de un usuario
     */
    List<Order> findByUserId(Long userId);

    /**
     * Find orders by user ID and status
     * Buscar pedidos por ID de usuario y estado
     */
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    /**
     * Find orders by status
     * Buscar pedidos por estado
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Find orders created between dates
     * Buscar pedidos creados entre fechas
     */
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find pending orders older than specified date
     * Buscar pedidos pendientes más antiguos que la fecha especificada
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND o.createdAt < :cutoffDate")
    List<Order> findPendingOrdersOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find user's most recent orders
     * Buscar los pedidos más recientes de un usuario
     */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    List<Order> findRecentOrdersByUserId(@Param("userId") Long userId);

    /**
     * Count orders by user ID
     * Contar pedidos por ID de usuario
     */
    long countByUserId(Long userId);

    /**
     * Count orders by status
     * Contar pedidos por estado
     */
    long countByStatus(OrderStatus status);

    /**
     * Check if user has orders
     * Verificar si un usuario tiene pedidos
     */
    boolean existsByUserId(Long userId);
}
