package com.microcommerce.order.mapper;

import com.microcommerce.order.dto.OrderDTO;
import com.microcommerce.order.dto.OrderItemDTO;
import com.microcommerce.order.dto.response.OrderResponse;
import com.microcommerce.order.dto.response.OrderResponse.OrderItemResponse;
import com.microcommerce.order.entity.Order;
import com.microcommerce.order.entity.Order.OrderStatus;
import com.microcommerce.order.entity.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for OrderMapper
 * Tests unitarios para OrderMapper
 */
@DisplayName("OrderMapper Tests")
class OrderMapperTest {

    private OrderMapper orderMapper;
    private Order order;
    private OrderDTO orderDTO;
    private OrderItem orderItem;
    private OrderItemDTO orderItemDTO;

    @BeforeEach
    void setUp() {
        orderMapper = new OrderMapper();

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
                .totalAmount(new BigDecimal("1999.98"))
                .shippingAddress("Av. Principal 123, Lima, Peru")
                .paymentMethod("CREDIT_CARD")
                .transactionId("txn-001")
                .notes("Test order")
                .items(List.of(orderItem))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .shippedAt(null)
                .deliveredAt(null)
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

    // --- toEntity tests ---

    @Test
    @DisplayName("toEntity - DTO valido - debe convertir correctamente")
    void toEntity_ValidDTO_ConvertsCorrectly() {
        // When
        Order result = orderMapper.toEntity(orderDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getShippingAddress()).isEqualTo("Av. Principal 123, Lima, Peru");
        assertThat(result.getPaymentMethod()).isEqualTo("CREDIT_CARD");
        assertThat(result.getNotes()).isEqualTo("Test order");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getId()).isNull();
    }

    @Test
    @DisplayName("toEntity - DTO null - debe retornar null")
    void toEntity_NullDTO_ReturnsNull() {
        // When
        Order result = orderMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("toEntity - DTO sin items - debe crear orden sin items")
    void toEntity_DTOWithoutItems_CreatesOrderWithoutItems() {
        // Given
        orderDTO.setItems(null);

        // When
        Order result = orderMapper.toEntity(orderDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEmpty();
    }

    // --- toItemEntity tests ---

    @Test
    @DisplayName("toItemEntity - DTO valido - debe convertir correctamente")
    void toItemEntity_ValidDTO_ConvertsCorrectly() {
        // When
        OrderItem result = orderMapper.toItemEntity(orderItemDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getProductName()).isEqualTo("Laptop HP");
        assertThat(result.getQuantity()).isEqualTo(2);
        assertThat(result.getUnitPrice()).isEqualByComparingTo(new BigDecimal("999.99"));
        assertThat(result.getSubtotal()).isEqualByComparingTo(new BigDecimal("1999.98"));
    }

    @Test
    @DisplayName("toItemEntity - DTO null - debe retornar null")
    void toItemEntity_NullDTO_ReturnsNull() {
        // When
        OrderItem result = orderMapper.toItemEntity(null);

        // Then
        assertThat(result).isNull();
    }

    // --- toResponse tests ---

    @Test
    @DisplayName("toResponse - entidad valida - debe convertir correctamente")
    void toResponse_ValidEntity_ConvertsCorrectly() {
        // When
        OrderResponse result = orderMapper.toResponse(order);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("order-001");
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1999.98"));
        assertThat(result.getShippingAddress()).isEqualTo("Av. Principal 123, Lima, Peru");
        assertThat(result.getPaymentMethod()).isEqualTo("CREDIT_CARD");
        assertThat(result.getTransactionId()).isEqualTo("txn-001");
        assertThat(result.getNotes()).isEqualTo("Test order");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItemCount()).isEqualTo(1);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("toResponse - entidad null - debe retornar null")
    void toResponse_NullEntity_ReturnsNull() {
        // When
        OrderResponse result = orderMapper.toResponse(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("toResponse - orden sin items - debe retornar lista vacia de items")
    void toResponse_OrderWithoutItems_ReturnsEmptyItemsList() {
        // Given
        order.setItems(null);

        // When
        OrderResponse result = orderMapper.toResponse(order);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getItemCount()).isEqualTo(0);
    }

    // --- toItemResponse tests ---

    @Test
    @DisplayName("toItemResponse - item valido - debe convertir correctamente")
    void toItemResponse_ValidItem_ConvertsCorrectly() {
        // When
        OrderItemResponse result = orderMapper.toItemResponse(orderItem);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getProductName()).isEqualTo("Laptop HP");
        assertThat(result.getQuantity()).isEqualTo(2);
        assertThat(result.getUnitPrice()).isEqualByComparingTo(new BigDecimal("999.99"));
        assertThat(result.getSubtotal()).isEqualByComparingTo(new BigDecimal("1999.98"));
    }

    @Test
    @DisplayName("toItemResponse - item null - debe retornar null")
    void toItemResponse_NullItem_ReturnsNull() {
        // When
        OrderItemResponse result = orderMapper.toItemResponse(null);

        // Then
        assertThat(result).isNull();
    }

    // --- updateEntityFromDto tests ---

    @Test
    @DisplayName("updateEntityFromDto - debe actualizar solo campos no nulos")
    void updateEntityFromDto_UpdatesOnlyNonNullFields() {
        // Given
        OrderDTO partialDTO = OrderDTO.builder()
                .shippingAddress("Nueva direccion 456")
                .paymentMethod("DEBIT_CARD")
                .build();

        // When
        orderMapper.updateEntityFromDto(partialDTO, order);

        // Then
        assertThat(order.getShippingAddress()).isEqualTo("Nueva direccion 456");
        assertThat(order.getPaymentMethod()).isEqualTo("DEBIT_CARD");
        assertThat(order.getNotes()).isEqualTo("Test order"); // no cambio
    }

    @Test
    @DisplayName("updateEntityFromDto - DTO null - no debe modificar entidad")
    void updateEntityFromDto_NullDTO_DoesNotModifyEntity() {
        // Given
        String originalAddress = order.getShippingAddress();

        // When
        orderMapper.updateEntityFromDto(null, order);

        // Then
        assertThat(order.getShippingAddress()).isEqualTo(originalAddress);
    }

    @Test
    @DisplayName("updateEntityFromDto - entidad null - no debe lanzar excepcion")
    void updateEntityFromDto_NullEntity_DoesNotThrowException() {
        // When & Then
        assertThatCode(() -> orderMapper.updateEntityFromDto(orderDTO, null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("updateEntityFromDto - solo notes - debe actualizar solo notes")
    void updateEntityFromDto_OnlyNotes_UpdatesOnlyNotes() {
        // Given
        OrderDTO partialDTO = OrderDTO.builder()
                .notes("Nuevas notas")
                .build();

        // When
        orderMapper.updateEntityFromDto(partialDTO, order);

        // Then
        assertThat(order.getNotes()).isEqualTo("Nuevas notas");
        assertThat(order.getShippingAddress()).isEqualTo("Av. Principal 123, Lima, Peru");
        assertThat(order.getPaymentMethod()).isEqualTo("CREDIT_CARD");
    }

    // --- toResponseList tests ---

    @Test
    @DisplayName("toResponseList - lista valida - debe convertir correctamente")
    void toResponseList_ValidList_ConvertsCorrectly() {
        // Given
        List<Order> orders = List.of(order, order);

        // When
        List<OrderResponse> result = orderMapper.toResponseList(orders);

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("toResponseList - lista null - debe retornar lista vacia")
    void toResponseList_NullList_ReturnsEmptyList() {
        // When
        List<OrderResponse> result = orderMapper.toResponseList(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("toResponseList - lista vacia - debe retornar lista vacia")
    void toResponseList_EmptyList_ReturnsEmptyList() {
        // When
        List<OrderResponse> result = orderMapper.toResponseList(Collections.emptyList());

        // Then
        assertThat(result).isEmpty();
    }
}
