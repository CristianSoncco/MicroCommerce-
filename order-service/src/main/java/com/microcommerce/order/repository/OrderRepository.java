package com.microcommerce.order.repository;

import com.microcommerce.order.entity.Order;
import com.microcommerce.order.entity.Order.OrderStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Repository (MongoDB)
 * Repositorio de Pedidos (MongoDB)
 *
 * Interface for order data access using Spring Data MongoDB.
 * Interface para acceso a datos de pedidos usando Spring Data MongoDB.
 */
@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

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
     * Buscar pedidos pendientes mas antiguos que la fecha especificada
     */
    @Query("{ 'status': 'PENDING', 'createdAt': { $lt: ?0 } }")
    List<Order> findPendingOrdersOlderThan(LocalDateTime cutoffDate);

    /**
     * Find user's most recent orders sorted by creation date descending
     * Buscar los pedidos mas recientes de un usuario ordenados por fecha descendente
     */
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

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
