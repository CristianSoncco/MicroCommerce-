package com.microcommerce.order.service;

import com.microcommerce.order.dto.OrderDTO;
import com.microcommerce.order.entity.Order;
import com.microcommerce.order.entity.Order.OrderStatus;
import com.microcommerce.order.event.OrderEventPublisher;
import com.microcommerce.order.exception.EmptyOrderException;
import com.microcommerce.order.exception.InvalidOrderStatusException;
import com.microcommerce.order.exception.OrderNotFoundException;
import com.microcommerce.order.mapper.OrderMapper;
import com.microcommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of OrderService
 * Implementacion de OrderService
 *
 * Handles all business logic for order management with MongoDB.
 * Gestiona toda la logica de negocio para gestion de pedidos con MongoDB.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderEventPublisher orderEventPublisher;

    /**
     * Valid status transitions map.
     * Mapa de transiciones de estado validas.
     *
     * PENDING -> PAID, CANCELLED
     * PAID -> PROCESSING, CANCELLED
     * PROCESSING -> SHIPPED, CANCELLED
     * SHIPPED -> DELIVERED
     * DELIVERED -> (terminal)
     * CANCELLED -> (terminal)
     */
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.PAID, OrderStatus.CANCELLED),
            OrderStatus.PAID, Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
            OrderStatus.PROCESSING, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
            OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, Set.of(),
            OrderStatus.CANCELLED, Set.of()
    );

    @Override
    public Order createOrder(OrderDTO orderDTO) {
        log.info("Creando nuevo pedido para usuario: {}", orderDTO.getUserId());

        if (orderDTO.getItems() == null || orderDTO.getItems().isEmpty()) {
            throw new EmptyOrderException();
        }

        Order order = orderMapper.toEntity(orderDTO);
        order.setStatus(OrderStatus.PENDING);

        // Calculate subtotals for each item and total amount
        order.getItems().forEach(item -> item.calculateSubtotal());
        order.calculateTotalAmount();

        Order savedOrder = orderRepository.save(order);

        log.info("Pedido creado satisfactoriamente con ID: {}", savedOrder.getId());

        // Publish ORDER_CREATED event for downstream consumers
        // Publicar evento ORDER_CREATED para consumidores downstream
        orderEventPublisher.publishOrderCreated(savedOrder);

        return savedOrder;
    }

    @Override
    public Order getOrderById(String id) {
        log.debug("Buscando pedido con ID: {}", id);

        return orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Pedido no encontrado con ID: {}", id);
                    return new OrderNotFoundException(id);
                });
    }

    @Override
    public List<Order> getAllOrders() {
        log.debug("Obteniendo todos los pedidos");
        return orderRepository.findAll();
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        log.debug("Obteniendo pedidos del usuario: {}", userId);
        return orderRepository.findByUserId(userId);
    }

    @Override
    public List<Order> getOrdersByUserIdAndStatus(Long userId, OrderStatus status) {
        log.debug("Obteniendo pedidos del usuario {} con estado {}", userId, status);
        return orderRepository.findByUserIdAndStatus(userId, status);
    }

    @Override
    public List<Order> getOrdersByStatus(OrderStatus status) {
        log.debug("Obteniendo pedidos con estado: {}", status);
        return orderRepository.findByStatus(status);
    }

    @Override
    public List<Order> getRecentOrdersByUserId(Long userId) {
        log.debug("Obteniendo pedidos recientes del usuario: {}", userId);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Obteniendo pedidos entre {} y {}", startDate, endDate);
        return orderRepository.findByCreatedAtBetween(startDate, endDate);
    }

    @Override
    public Order updateOrder(String id, OrderDTO orderDTO) {
        log.info("Actualizando pedido con ID: {}", id);

        Order existingOrder = getOrderById(id);

        // Only allow updates on PENDING orders
        if (existingOrder.getStatus() != OrderStatus.PENDING) {
            log.error("No se puede actualizar el pedido {} en estado {}", id, existingOrder.getStatus());
            throw new InvalidOrderStatusException(existingOrder.getStatus(), OrderStatus.PENDING);
        }

        orderMapper.updateEntityFromDto(orderDTO, existingOrder);

        // If items are provided, update them and recalculate totals
        if (orderDTO.getItems() != null && !orderDTO.getItems().isEmpty()) {
            List<com.microcommerce.order.entity.OrderItem> newItems = orderDTO.getItems().stream()
                    .map(orderMapper::toItemEntity)
                    .toList();
            existingOrder.setItems(newItems);
            existingOrder.getItems().forEach(item -> item.calculateSubtotal());
            existingOrder.calculateTotalAmount();
        }

        Order updatedOrder = orderRepository.save(existingOrder);

        log.info("Pedido actualizado satisfactoriamente: {}", id);
        return updatedOrder;
    }

    @Override
    public Order updateOrderStatus(String id, OrderStatus newStatus) {
        log.info("Actualizando estado del pedido {} a {}", id, newStatus);

        Order order = getOrderById(id);
        OrderStatus currentStatus = order.getStatus();

        // Validate status transition
        Set<OrderStatus> allowedTransitions = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowedTransitions.contains(newStatus)) {
            log.error("Transicion de estado invalida de {} a {} para pedido {}", currentStatus, newStatus, id);
            throw new InvalidOrderStatusException(currentStatus, newStatus);
        }

        order.setStatus(newStatus);

        // Set timestamp for specific status transitions
        if (newStatus == OrderStatus.SHIPPED) {
            order.setShippedAt(LocalDateTime.now());
        } else if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }

        Order updatedOrder = orderRepository.save(order);

        log.info("Estado del pedido {} actualizado de {} a {}", id, currentStatus, newStatus);

        // Publish ORDER_CANCELLED when the order is cancelled
        // Publicar ORDER_CANCELLED cuando el pedido se cancela
        if (newStatus == OrderStatus.CANCELLED) {
            orderEventPublisher.publishOrderCancelled(updatedOrder);
        }

        return updatedOrder;
    }

    @Override
    public Order cancelOrder(String id) {
        log.info("Cancelando pedido: {}", id);
        return updateOrderStatus(id, OrderStatus.CANCELLED);
    }

    @Override
    public void deleteOrder(String id) {
        log.info("Eliminando pedido con ID: {}", id);

        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException(id);
        }

        orderRepository.deleteById(id);

        log.info("Pedido eliminado satisfactoriamente: {}", id);
    }

    @Override
    public long countOrdersByUserId(Long userId) {
        log.debug("Contando pedidos del usuario: {}", userId);
        return orderRepository.countByUserId(userId);
    }

    @Override
    public long countOrdersByStatus(OrderStatus status) {
        log.debug("Contando pedidos con estado: {}", status);
        return orderRepository.countByStatus(status);
    }

    @Override
    public boolean userHasOrders(Long userId) {
        log.debug("Verificando si el usuario {} tiene pedidos", userId);
        return orderRepository.existsByUserId(userId);
    }
}
