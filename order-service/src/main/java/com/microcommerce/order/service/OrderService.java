package com.microcommerce.order.service;

import com.microcommerce.order.dto.OrderDTO;
import com.microcommerce.order.entity.Order;
import com.microcommerce.order.entity.Order.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for Order operations
 * Interfaz de servicio para operaciones de Pedidos
 *
 * Defines the business logic contract for order management.
 * Define el contrato de logica de negocio para gestion de pedidos.
 */
public interface OrderService {

    /**
     * Create a new order
     * Crear un nuevo pedido
     */
    Order createOrder(OrderDTO orderDTO);

    /**
     * Get order by ID
     * Obtener pedido por ID
     */
    Order getOrderById(String id);

    /**
     * Get all orders
     * Obtener todos los pedidos
     */
    List<Order> getAllOrders();

    /**
     * Get orders by user ID
     * Obtener pedidos por ID de usuario
     */
    List<Order> getOrdersByUserId(Long userId);

    /**
     * Get orders by user ID and status
     * Obtener pedidos por ID de usuario y estado
     */
    List<Order> getOrdersByUserIdAndStatus(Long userId, OrderStatus status);

    /**
     * Get orders by status
     * Obtener pedidos por estado
     */
    List<Order> getOrdersByStatus(OrderStatus status);

    /**
     * Get user's most recent orders
     * Obtener los pedidos mas recientes de un usuario
     */
    List<Order> getRecentOrdersByUserId(Long userId);

    /**
     * Get orders created between dates
     * Obtener pedidos creados entre fechas
     */
    List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Update order details (shipping address, payment method, notes)
     * Actualizar detalles del pedido (direccion de envio, metodo de pago, notas)
     */
    Order updateOrder(String id, OrderDTO orderDTO);

    /**
     * Update order status with validation of allowed transitions
     * Actualizar estado del pedido con validacion de transiciones permitidas
     */
    Order updateOrderStatus(String id, OrderStatus newStatus);

    /**
     * Cancel an order
     * Cancelar un pedido
     */
    Order cancelOrder(String id);

    /**
     * Delete an order
     * Eliminar un pedido
     */
    void deleteOrder(String id);

    /**
     * Count orders by user ID
     * Contar pedidos por ID de usuario
     */
    long countOrdersByUserId(Long userId);

    /**
     * Count orders by status
     * Contar pedidos por estado
     */
    long countOrdersByStatus(OrderStatus status);

    /**
     * Check if user has any orders
     * Verificar si un usuario tiene pedidos
     */
    boolean userHasOrders(Long userId);
}
