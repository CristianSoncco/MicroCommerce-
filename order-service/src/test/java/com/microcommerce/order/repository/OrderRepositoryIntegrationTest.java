package com.microcommerce.order.repository;

import com.microcommerce.order.config.MongoConfig;
import com.microcommerce.order.entity.Order;
import com.microcommerce.order.entity.Order.OrderStatus;
import com.microcommerce.order.entity.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for OrderRepository using TestContainers
 * Tests de integracion para OrderRepository usando TestContainers
 */
@DataMongoTest
@Testcontainers
@ActiveProfiles("test-tc")
@Import(MongoConfig.class)
@DisplayName("OrderRepository Integration Tests")
class OrderRepositoryIntegrationTest {

    @Container
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);
    }

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("Guardar pedido - debe persistir correctamente")
    void saveOrder_ShouldPersistCorrectly() {
        // Given
        Order order = createOrder(1L, OrderStatus.PENDING, "Av. Principal 123");

        // When
        Order saved = orderRepository.save(order);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findById - debe encontrar pedido existente")
    void findById_ShouldFindExistingOrder() {
        // Given
        Order saved = orderRepository.save(createOrder(1L, OrderStatus.PENDING, "Av. Principal 123"));

        // When
        Optional<Order> found = orderRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById - ID no existente - debe retornar vacio")
    void findById_NonExistingId_ShouldReturnEmpty() {
        // When
        Optional<Order> found = orderRepository.findById("nonexistent-id");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findByUserId - debe retornar pedidos del usuario")
    void findByUserId_ShouldReturnUserOrders() {
        // Given
        createAndSaveOrder(1L, OrderStatus.PENDING, "Direccion 1");
        createAndSaveOrder(1L, OrderStatus.PAID, "Direccion 2");
        createAndSaveOrder(2L, OrderStatus.PENDING, "Direccion 3");

        // When
        List<Order> user1Orders = orderRepository.findByUserId(1L);
        List<Order> user2Orders = orderRepository.findByUserId(2L);

        // Then
        assertThat(user1Orders).hasSize(2);
        assertThat(user2Orders).hasSize(1);
    }

    @Test
    @DisplayName("findByUserIdAndStatus - debe filtrar por usuario y estado")
    void findByUserIdAndStatus_ShouldFilterByUserAndStatus() {
        // Given
        createAndSaveOrder(1L, OrderStatus.PENDING, "Direccion 1");
        createAndSaveOrder(1L, OrderStatus.PAID, "Direccion 2");
        createAndSaveOrder(1L, OrderStatus.PENDING, "Direccion 3");

        // When
        List<Order> pendingOrders = orderRepository.findByUserIdAndStatus(1L, OrderStatus.PENDING);
        List<Order> paidOrders = orderRepository.findByUserIdAndStatus(1L, OrderStatus.PAID);

        // Then
        assertThat(pendingOrders).hasSize(2);
        assertThat(paidOrders).hasSize(1);
    }

    @Test
    @DisplayName("findByStatus - debe retornar pedidos por estado")
    void findByStatus_ShouldReturnOrdersByStatus() {
        // Given
        createAndSaveOrder(1L, OrderStatus.PENDING, "Direccion 1");
        createAndSaveOrder(2L, OrderStatus.PENDING, "Direccion 2");
        createAndSaveOrder(1L, OrderStatus.PAID, "Direccion 3");

        // When
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);

        // Then
        assertThat(pendingOrders).hasSize(2);
        assertThat(pendingOrders).allMatch(o -> o.getStatus() == OrderStatus.PENDING);
    }

    @Test
    @DisplayName("findByCreatedAtBetween - debe retornar pedidos en rango de fechas")
    void findByCreatedAtBetween_ShouldReturnOrdersInDateRange() {
        // Given
        createAndSaveOrder(1L, OrderStatus.PENDING, "Direccion 1");
        createAndSaveOrder(2L, OrderStatus.PAID, "Direccion 2");

        LocalDateTime start = LocalDateTime.now().minusMinutes(5);
        LocalDateTime end = LocalDateTime.now().plusMinutes(5);

        // When
        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);

        // Then
        assertThat(orders).hasSize(2);
    }

    @Test
    @DisplayName("findByUserIdOrderByCreatedAtDesc - debe ordenar por fecha descendente")
    void findByUserIdOrderByCreatedAtDesc_ShouldOrderByDateDesc() throws InterruptedException {
        // Given
        Order first = createAndSaveOrder(1L, OrderStatus.PENDING, "Direccion 1");
        Thread.sleep(50);
        Order second = createAndSaveOrder(1L, OrderStatus.PAID, "Direccion 2");

        // When
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(1L);

        // Then
        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getCreatedAt()).isAfterOrEqualTo(orders.get(1).getCreatedAt());
    }

    @Test
    @DisplayName("countByUserId - debe contar pedidos del usuario")
    void countByUserId_ShouldCountUserOrders() {
        // Given
        createAndSaveOrder(1L, OrderStatus.PENDING, "Direccion 1");
        createAndSaveOrder(1L, OrderStatus.PAID, "Direccion 2");
        createAndSaveOrder(2L, OrderStatus.PENDING, "Direccion 3");

        // When
        long user1Count = orderRepository.countByUserId(1L);
        long user2Count = orderRepository.countByUserId(2L);
        long user3Count = orderRepository.countByUserId(3L);

        // Then
        assertThat(user1Count).isEqualTo(2);
        assertThat(user2Count).isEqualTo(1);
        assertThat(user3Count).isEqualTo(0);
    }

    @Test
    @DisplayName("countByStatus - debe contar pedidos por estado")
    void countByStatus_ShouldCountOrdersByStatus() {
        // Given
        createAndSaveOrder(1L, OrderStatus.PENDING, "Direccion 1");
        createAndSaveOrder(2L, OrderStatus.PENDING, "Direccion 2");
        createAndSaveOrder(1L, OrderStatus.PAID, "Direccion 3");

        // When
        long pendingCount = orderRepository.countByStatus(OrderStatus.PENDING);
        long paidCount = orderRepository.countByStatus(OrderStatus.PAID);
        long cancelledCount = orderRepository.countByStatus(OrderStatus.CANCELLED);

        // Then
        assertThat(pendingCount).isEqualTo(2);
        assertThat(paidCount).isEqualTo(1);
        assertThat(cancelledCount).isEqualTo(0);
    }

    @Test
    @DisplayName("existsByUserId - debe verificar si el usuario tiene pedidos")
    void existsByUserId_ShouldCheckIfUserHasOrders() {
        // Given
        createAndSaveOrder(1L, OrderStatus.PENDING, "Direccion 1");

        // When
        boolean exists = orderRepository.existsByUserId(1L);
        boolean notExists = orderRepository.existsByUserId(999L);

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Actualizar pedido - debe persistir cambios")
    void updateOrder_ShouldPersistChanges() {
        // Given
        Order saved = createAndSaveOrder(1L, OrderStatus.PENDING, "Direccion original");
        String orderId = saved.getId();

        // When
        saved.setStatus(OrderStatus.PAID);
        saved.setShippingAddress("Direccion actualizada");
        orderRepository.save(saved);

        // Then
        Order updated = orderRepository.findById(orderId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(updated.getShippingAddress()).isEqualTo("Direccion actualizada");
    }

    @Test
    @DisplayName("Eliminar pedido - debe remover de base de datos")
    void deleteOrder_ShouldRemoveFromDatabase() {
        // Given
        Order saved = createAndSaveOrder(1L, OrderStatus.PENDING, "Direccion 1");
        String orderId = saved.getId();

        // When
        orderRepository.deleteById(orderId);

        // Then
        Optional<Order> deleted = orderRepository.findById(orderId);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("findByUserId - usuario sin pedidos - debe retornar lista vacia")
    void findByUserId_UserWithoutOrders_ShouldReturnEmptyList() {
        // When
        List<Order> orders = orderRepository.findByUserId(999L);

        // Then
        assertThat(orders).isEmpty();
    }

    @Test
    @DisplayName("findByStatus - estado sin pedidos - debe retornar lista vacia")
    void findByStatus_StatusWithoutOrders_ShouldReturnEmptyList() {
        // When
        List<Order> orders = orderRepository.findByStatus(OrderStatus.SHIPPED);

        // Then
        assertThat(orders).isEmpty();
    }

    @Test
    @DisplayName("Guardar pedido con items - debe persistir items embebidos")
    void saveOrderWithItems_ShouldPersistEmbeddedItems() {
        // Given
        Order order = createOrder(1L, OrderStatus.PENDING, "Av. Principal 123");

        // When
        Order saved = orderRepository.save(order);

        // Then
        Order found = orderRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getItems()).hasSize(1);
        assertThat(found.getItems().get(0).getProductName()).isEqualTo("Laptop HP");
        assertThat(found.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    // Helper methods

    private Order createOrder(Long userId, OrderStatus status, String address) {
        OrderItem item = OrderItem.builder()
                .productId(1L)
                .productName("Laptop HP")
                .quantity(2)
                .unitPrice(new BigDecimal("999.99"))
                .subtotal(new BigDecimal("1999.98"))
                .build();

        return Order.builder()
                .userId(userId)
                .status(status)
                .shippingAddress(address)
                .paymentMethod("CREDIT_CARD")
                .notes("Test order")
                .items(new java.util.ArrayList<>(List.of(item)))
                .totalAmount(new BigDecimal("1999.98"))
                .build();
    }

    private Order createAndSaveOrder(Long userId, OrderStatus status, String address) {
        return orderRepository.save(createOrder(userId, status, address));
    }
}
