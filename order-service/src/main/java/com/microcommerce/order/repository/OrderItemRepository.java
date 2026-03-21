package com.microcommerce.order.repository;

import com.microcommerce.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * OrderItem Repository
 * Repositorio de Items de Pedido
 * 
 * Interface para acceso a datos de items de pedidos usando Spring Data JPA.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find all items for a specific order
     * Buscar todos los items de un pedido específico
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * Find all items for a specific product
     * Buscar todos los items de un producto específico
     */
    List<OrderItem> findByProductId(Long productId);

    /**
     * Count items by product ID
     * Contar items por ID de producto
     */
    long countByProductId(Long productId);

    /**
     * Get total quantity ordered for a product
     * Obtener cantidad total pedida de un producto
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.productId = :productId")
    Long getTotalQuantityByProductId(@Param("productId") Long productId);

    /**
     * Find items by order ID and product ID
     * Buscar items por ID de pedido e ID de producto
     */
    List<OrderItem> findByOrderIdAndProductId(Long orderId, Long productId);
}
