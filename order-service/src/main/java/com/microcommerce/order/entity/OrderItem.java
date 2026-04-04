package com.microcommerce.order.entity;

import lombok.*;

import java.math.BigDecimal;

/**
 * OrderItem embedded document (MongoDB)
 * Documento embebido de Item de Pedido (MongoDB)
 *
 * Represents an individual item within an order.
 * Stored as an embedded document inside the Order document.
 *
 * Representa un item individual dentro de un pedido.
 * Se almacena como documento embebido dentro del documento Order.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    private Long productId;

    private String productName;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal subtotal;

    /**
     * Calculate subtotal (quantity * unitPrice)
     * Calcular subtotal (cantidad * precioUnitario)
     */
    public void calculateSubtotal() {
        if (quantity != null && unitPrice != null) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem that)) return false;
        return productId != null && productId.equals(that.getProductId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
