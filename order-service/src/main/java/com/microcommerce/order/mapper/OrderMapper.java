package com.microcommerce.order.mapper;

import com.microcommerce.order.dto.OrderDTO;
import com.microcommerce.order.dto.OrderItemDTO;
import com.microcommerce.order.dto.response.OrderResponse;
import com.microcommerce.order.dto.response.OrderResponse.OrderItemResponse;
import com.microcommerce.order.entity.Order;
import com.microcommerce.order.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Order entity and DTOs
 * Mapper para convertir entre entidad Order y DTOs
 */
@Component
public class OrderMapper {

    /**
     * Convert OrderDTO to Order entity
     * Convertir OrderDTO a entidad Order
     */
    public Order toEntity(OrderDTO dto) {
        if (dto == null) {
            return null;
        }

        Order order = Order.builder()
                .userId(dto.getUserId())
                .shippingAddress(dto.getShippingAddress())
                .paymentMethod(dto.getPaymentMethod())
                .notes(dto.getNotes())
                .build();

        if (dto.getItems() != null) {
            List<OrderItem> items = dto.getItems().stream()
                    .map(this::toItemEntity)
                    .collect(Collectors.toList());
            order.setItems(items);
        }

        return order;
    }

    /**
     * Convert OrderItemDTO to OrderItem entity
     * Convertir OrderItemDTO a entidad OrderItem
     */
    public OrderItem toItemEntity(OrderItemDTO dto) {
        if (dto == null) {
            return null;
        }

        OrderItem item = OrderItem.builder()
                .productId(dto.getProductId())
                .productName(dto.getProductName())
                .quantity(dto.getQuantity())
                .unitPrice(dto.getUnitPrice())
                .build();

        item.calculateSubtotal();
        return item;
    }

    /**
     * Convert Order entity to OrderResponse
     * Convertir entidad Order a OrderResponse
     */
    public OrderResponse toResponse(Order order) {
        if (order == null) {
            return null;
        }

        List<OrderItemResponse> itemResponses = order.getItems() != null
                ? order.getItems().stream()
                    .map(this::toItemResponse)
                    .collect(Collectors.toList())
                : Collections.emptyList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(order.getPaymentMethod())
                .transactionId(order.getTransactionId())
                .notes(order.getNotes())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .itemCount(itemResponses.size())
                .build();
    }

    /**
     * Convert OrderItem entity to OrderItemResponse
     * Convertir entidad OrderItem a OrderItemResponse
     */
    public OrderItemResponse toItemResponse(OrderItem item) {
        if (item == null) {
            return null;
        }

        return OrderItemResponse.builder()
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }

    /**
     * Update Order entity from OrderDTO (partial update)
     * Actualizar entidad Order desde OrderDTO (actualizacion parcial)
     */
    public void updateEntityFromDto(OrderDTO dto, Order order) {
        if (dto == null || order == null) {
            return;
        }

        if (dto.getShippingAddress() != null) {
            order.setShippingAddress(dto.getShippingAddress());
        }
        if (dto.getPaymentMethod() != null) {
            order.setPaymentMethod(dto.getPaymentMethod());
        }
        if (dto.getNotes() != null) {
            order.setNotes(dto.getNotes());
        }
    }

    /**
     * Convert list of Order entities to list of OrderResponse
     * Convertir lista de entidades Order a lista de OrderResponse
     */
    public List<OrderResponse> toResponseList(List<Order> orders) {
        if (orders == null) {
            return Collections.emptyList();
        }
        return orders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
