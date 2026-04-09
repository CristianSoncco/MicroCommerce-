package com.microcommerce.order.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order Document (MongoDB)
 * Documento de Pedido (MongoDB)
 *
 * Represents an order placed by a user.
 * OrderItems are embedded as a list inside the order document.
 *
 * Representa un pedido realizado por un usuario.
 * Los items del pedido se almacenan embebidos dentro del documento.
 *
 * Possible statuses / Estados posibles:
 * - PENDING: Order created, awaiting payment / Pedido creado, esperando pago
 * - PAID: Payment confirmed / Pago confirmado
 * - PROCESSING: Being prepared / En preparacion
 * - SHIPPED: Shipped / Enviado
 * - DELIVERED: Delivered / Entregado
 * - CANCELLED: Cancelled / Cancelado
 */
@Document(collection = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    private String id;

    @Indexed
    private Long userId;

    @Indexed
    private OrderStatus status;

    private BigDecimal totalAmount;

    private String shippingAddress;

    private String paymentMethod;

    private String transactionId;

    private String notes;

    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @CreatedDate
    @Indexed
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime shippedAt;

    private LocalDateTime deliveredAt;

    /**
     * Add order item helper method
     * Metodo auxiliar para agregar items al pedido
     */
    public void addItem(OrderItem item) {
        items.add(item);
    }

    /**
     * Remove order item helper method
     * Metodo auxiliar para remover items del pedido
     */
    public void removeItem(OrderItem item) {
        items.remove(item);
    }

    /**
     * Calculate total amount from items
     * Calcular monto total desde los items
     */
    public void calculateTotalAmount() {
        this.totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Order Status Enum
     * Enum de Estados del Pedido
     */
    public enum OrderStatus {
        PENDING,
        PAID,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }
}
