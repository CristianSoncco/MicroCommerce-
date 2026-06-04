package com.microcommerce.order.service;

import com.microcommerce.order.dto.OrderDTO;
import com.microcommerce.order.dto.OrderItemDTO;
import com.microcommerce.order.entity.Order;
import com.microcommerce.order.entity.Order.OrderStatus;
import com.microcommerce.order.entity.OrderItem;
import com.microcommerce.order.event.OrderEventPublisher;
import com.microcommerce.order.exception.EmptyOrderException;
import com.microcommerce.order.exception.InvalidOrderStatusException;
import com.microcommerce.order.exception.OrderNotFoundException;
import com.microcommerce.order.mapper.OrderMapper;
import com.microcommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderServiceImpl
 * Tests unitarios para OrderServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl Tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private OrderDTO orderDTO;
    private OrderItem orderItem;
    private OrderItemDTO orderItemDTO;

    @BeforeEach
    void setUp() {
        orderItem = OrderItem.builder()
                .productId(1L)
                .productName("Laptop HP")
                .quantity(2)
                .unitPrice(new BigDecimal("999.99"))
                .subtotal(new BigDecimal("1999.98"))
                .build();

        order = Order.builder()
                .id("order-001")
                .userId(1L)
                .status(OrderStatus.PENDING)
                .shippingAddress("Av. Principal 123, Lima, Peru")
                .paymentMethod("CREDIT_CARD")
                .notes("Test order")
                .items(new java.util.ArrayList<>(List.of(orderItem)))
                .totalAmount(new BigDecimal("1999.98"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        orderItemDTO = OrderItemDTO.builder()
                .productId(1L)
                .productName("Laptop HP")
                .quantity(2)
                .unitPrice(new BigDecimal("999.99"))
                .build();

        orderDTO = OrderDTO.builder()
                .userId(1L)
                .shippingAddress("Av. Principal 123, Lima, Peru")
                .paymentMethod("CREDIT_CARD")
                .notes("Test order")
                .items(List.of(orderItemDTO))
                .build();
    }

    // --- createOrder tests ---

    @Test
    @DisplayName("createOrder - DTO valido - debe retornar pedido creado")
    void createOrder_ValidDTO_ReturnsOrder() {
        // Given
        when(orderMapper.toEntity(any(OrderDTO.class))).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        Order result = orderService.createOrder(orderDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("order-001");
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(orderMapper).toEntity(orderDTO);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("createOrder - items null - debe lanzar EmptyOrderException")
    void createOrder_NullItems_ThrowsEmptyOrderException() {
        // Given
        orderDTO.setItems(null);

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(orderDTO))
                .isInstanceOf(EmptyOrderException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrder - items vacios - debe lanzar EmptyOrderException")
    void createOrder_EmptyItems_ThrowsEmptyOrderException() {
        // Given
        orderDTO.setItems(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(orderDTO))
                .isInstanceOf(EmptyOrderException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrder - debe establecer estado PENDING")
    void createOrder_ShouldSetStatusPending() {
        // Given
        when(orderMapper.toEntity(any(OrderDTO.class))).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        Order result = orderService.createOrder(orderDTO);

        // Then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    // --- getOrderById tests ---

    @Test
    @DisplayName("getOrderById - ID existente - debe retornar pedido")
    void getOrderById_ExistingId_ReturnsOrder() {
        // Given
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));

        // When
        Order result = orderService.getOrderById("order-001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("order-001");
        verify(orderRepository).findById("order-001");
    }

    @Test
    @DisplayName("getOrderById - ID no existente - debe lanzar OrderNotFoundException")
    void getOrderById_NonExistingId_ThrowsException() {
        // Given
        when(orderRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderById("nonexistent"))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("nonexistent");
    }

    // --- getAllOrders tests ---

    @Test
    @DisplayName("getAllOrders - debe retornar lista de pedidos")
    void getAllOrders_ReturnsOrderList() {
        // Given
        List<Order> orders = Arrays.asList(order, order);
        when(orderRepository.findAll()).thenReturn(orders);

        // When
        List<Order> result = orderService.getAllOrders();

        // Then
        assertThat(result).hasSize(2);
        verify(orderRepository).findAll();
    }

    @Test
    @DisplayName("getAllOrders - lista vacia - debe retornar lista vacia")
    void getAllOrders_EmptyList_ReturnsEmptyList() {
        // Given
        when(orderRepository.findAll()).thenReturn(List.of());

        // When
        List<Order> result = orderService.getAllOrders();

        // Then
        assertThat(result).isEmpty();
    }

    // --- getOrdersByUserId tests ---

    @Test
    @DisplayName("getOrdersByUserId - usuario con pedidos - debe retornar lista")
    void getOrdersByUserId_UserWithOrders_ReturnsList() {
        // Given
        when(orderRepository.findByUserId(1L)).thenReturn(List.of(order));

        // When
        List<Order> result = orderService.getOrdersByUserId(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(orderRepository).findByUserId(1L);
    }

    // --- getOrdersByUserIdAndStatus tests ---

    @Test
    @DisplayName("getOrdersByUserIdAndStatus - debe filtrar por usuario y estado")
    void getOrdersByUserIdAndStatus_ReturnsFilteredList() {
        // Given
        when(orderRepository.findByUserIdAndStatus(1L, OrderStatus.PENDING))
                .thenReturn(List.of(order));

        // When
        List<Order> result = orderService.getOrdersByUserIdAndStatus(1L, OrderStatus.PENDING);

        // Then
        assertThat(result).hasSize(1);
        verify(orderRepository).findByUserIdAndStatus(1L, OrderStatus.PENDING);
    }

    // --- getOrdersByStatus tests ---

    @Test
    @DisplayName("getOrdersByStatus - debe retornar pedidos por estado")
    void getOrdersByStatus_ReturnsOrdersByStatus() {
        // Given
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(List.of(order));

        // When
        List<Order> result = orderService.getOrdersByStatus(OrderStatus.PENDING);

        // Then
        assertThat(result).hasSize(1);
        verify(orderRepository).findByStatus(OrderStatus.PENDING);
    }

    // --- getRecentOrdersByUserId tests ---

    @Test
    @DisplayName("getRecentOrdersByUserId - debe retornar pedidos recientes del usuario")
    void getRecentOrdersByUserId_ReturnsRecentOrders() {
        // Given
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(order));

        // When
        List<Order> result = orderService.getRecentOrdersByUserId(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(orderRepository).findByUserIdOrderByCreatedAtDesc(1L);
    }

    // --- getOrdersByDateRange tests ---

    @Test
    @DisplayName("getOrdersByDateRange - debe retornar pedidos en rango de fechas")
    void getOrdersByDateRange_ReturnsOrdersInRange() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        when(orderRepository.findByCreatedAtBetween(start, end)).thenReturn(List.of(order));

        // When
        List<Order> result = orderService.getOrdersByDateRange(start, end);

        // Then
        assertThat(result).hasSize(1);
        verify(orderRepository).findByCreatedAtBetween(start, end);
    }

    // --- updateOrder tests ---

    @Test
    @DisplayName("updateOrder - pedido PENDING - debe actualizar correctamente")
    void updateOrder_PendingOrder_UpdatesSuccessfully() {
        // Given
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        Order result = orderService.updateOrder("order-001", orderDTO);

        // Then
        assertThat(result).isNotNull();
        verify(orderMapper).updateEntityFromDto(orderDTO, order);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("updateOrder - pedido no PENDING - debe lanzar InvalidOrderStatusException")
    void updateOrder_NonPendingOrder_ThrowsException() {
        // Given
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));

        // When & Then
        assertThatThrownBy(() -> orderService.updateOrder("order-001", orderDTO))
                .isInstanceOf(InvalidOrderStatusException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateOrder - ID no existente - debe lanzar OrderNotFoundException")
    void updateOrder_NonExistingId_ThrowsException() {
        // Given
        when(orderRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.updateOrder("nonexistent", orderDTO))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    @DisplayName("updateOrder - con nuevos items - debe recalcular totales")
    void updateOrder_WithNewItems_RecalculatesTotals() {
        // Given
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toItemEntity(any(OrderItemDTO.class))).thenReturn(orderItem);

        // When
        Order result = orderService.updateOrder("order-001", orderDTO);

        // Then
        assertThat(result).isNotNull();
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("updateOrder - sin items en DTO - no debe actualizar items")
    void updateOrder_WithoutItems_ShouldNotUpdateItems() {
        // Given
        orderDTO.setItems(null);
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        Order result = orderService.updateOrder("order-001", orderDTO);

        // Then
        assertThat(result).isNotNull();
        verify(orderMapper, never()).toItemEntity(any());
    }

    // --- updateOrderStatus tests ---

    @Test
    @DisplayName("updateOrderStatus - PENDING a PAID - transicion valida")
    void updateOrderStatus_PendingToPaid_ValidTransition() {
        // Given
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        Order result = orderService.updateOrderStatus("order-001", OrderStatus.PAID);

        // Then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("updateOrderStatus - PENDING a CANCELLED - transicion valida")
    void updateOrderStatus_PendingToCancelled_ValidTransition() {
        // Given
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        Order result = orderService.updateOrderStatus("order-001", OrderStatus.CANCELLED);

        // Then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("updateOrderStatus - PENDING a SHIPPED - transicion invalida")
    void updateOrderStatus_PendingToShipped_InvalidTransition() {
        // Given
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));

        // When & Then
        assertThatThrownBy(() -> orderService.updateOrderStatus("order-001", OrderStatus.SHIPPED))
                .isInstanceOf(InvalidOrderStatusException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateOrderStatus - DELIVERED terminal - no permite transiciones")
    void updateOrderStatus_DeliveredTerminal_NoTransitionsAllowed() {
        // Given
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));

        // When & Then
        assertThatThrownBy(() -> orderService.updateOrderStatus("order-001", OrderStatus.CANCELLED))
                .isInstanceOf(InvalidOrderStatusException.class);
    }

    @Test
    @DisplayName("updateOrderStatus - CANCELLED terminal - no permite transiciones")
    void updateOrderStatus_CancelledTerminal_NoTransitionsAllowed() {
        // Given
        order.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));

        // When & Then
        assertThatThrownBy(() -> orderService.updateOrderStatus("order-001", OrderStatus.PENDING))
                .isInstanceOf(InvalidOrderStatusException.class);
    }

    @Test
    @DisplayName("updateOrderStatus - a SHIPPED - debe establecer shippedAt")
    void updateOrderStatus_ToShipped_SetsShippedAt() {
        // Given
        order.setStatus(OrderStatus.PROCESSING);
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        Order result = orderService.updateOrderStatus("order-001", OrderStatus.SHIPPED);

        // Then
        assertThat(result.getShippedAt()).isNotNull();
    }

    @Test
    @DisplayName("updateOrderStatus - a DELIVERED - debe establecer deliveredAt")
    void updateOrderStatus_ToDelivered_SetsDeliveredAt() {
        // Given
        order.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        Order result = orderService.updateOrderStatus("order-001", OrderStatus.DELIVERED);

        // Then
        assertThat(result.getDeliveredAt()).isNotNull();
    }

    @Test
    @DisplayName("updateOrderStatus - PAID a PROCESSING - transicion valida")
    void updateOrderStatus_PaidToProcessing_ValidTransition() {
        // Given
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        Order result = orderService.updateOrderStatus("order-001", OrderStatus.PROCESSING);

        // Then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PROCESSING);
    }

    // --- cancelOrder tests ---

    @Test
    @DisplayName("cancelOrder - pedido PENDING - debe cancelar")
    void cancelOrder_PendingOrder_CancelsSuccessfully() {
        // Given
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        Order result = orderService.cancelOrder("order-001");

        // Then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancelOrder - pedido DELIVERED - debe lanzar excepcion")
    void cancelOrder_DeliveredOrder_ThrowsException() {
        // Given
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById("order-001")).thenReturn(Optional.of(order));

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder("order-001"))
                .isInstanceOf(InvalidOrderStatusException.class);
    }

    // --- deleteOrder tests ---

    @Test
    @DisplayName("deleteOrder - ID existente - debe eliminar pedido")
    void deleteOrder_ExistingId_DeletesOrder() {
        // Given
        when(orderRepository.existsById("order-001")).thenReturn(true);

        // When
        orderService.deleteOrder("order-001");

        // Then
        verify(orderRepository).deleteById("order-001");
    }

    @Test
    @DisplayName("deleteOrder - ID no existente - debe lanzar OrderNotFoundException")
    void deleteOrder_NonExistingId_ThrowsException() {
        // Given
        when(orderRepository.existsById("nonexistent")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> orderService.deleteOrder("nonexistent"))
                .isInstanceOf(OrderNotFoundException.class);
        verify(orderRepository, never()).deleteById(anyString());
    }

    // --- countOrdersByUserId tests ---

    @Test
    @DisplayName("countOrdersByUserId - debe retornar cantidad correcta")
    void countOrdersByUserId_ReturnsCorrectCount() {
        // Given
        when(orderRepository.countByUserId(1L)).thenReturn(5L);

        // When
        long result = orderService.countOrdersByUserId(1L);

        // Then
        assertThat(result).isEqualTo(5L);
        verify(orderRepository).countByUserId(1L);
    }

    // --- countOrdersByStatus tests ---

    @Test
    @DisplayName("countOrdersByStatus - debe retornar cantidad correcta")
    void countOrdersByStatus_ReturnsCorrectCount() {
        // Given
        when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(3L);

        // When
        long result = orderService.countOrdersByStatus(OrderStatus.PENDING);

        // Then
        assertThat(result).isEqualTo(3L);
        verify(orderRepository).countByStatus(OrderStatus.PENDING);
    }

    // --- userHasOrders tests ---

    @Test
    @DisplayName("userHasOrders - usuario con pedidos - debe retornar true")
    void userHasOrders_UserWithOrders_ReturnsTrue() {
        // Given
        when(orderRepository.existsByUserId(1L)).thenReturn(true);

        // When
        boolean result = orderService.userHasOrders(1L);

        // Then
        assertThat(result).isTrue();
        verify(orderRepository).existsByUserId(1L);
    }

    @Test
    @DisplayName("userHasOrders - usuario sin pedidos - debe retornar false")
    void userHasOrders_UserWithoutOrders_ReturnsFalse() {
        // Given
        when(orderRepository.existsByUserId(999L)).thenReturn(false);

        // When
        boolean result = orderService.userHasOrders(999L);

        // Then
        assertThat(result).isFalse();
    }
}
