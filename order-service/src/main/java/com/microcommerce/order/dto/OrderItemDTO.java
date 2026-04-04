package com.microcommerce.order.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * OrderItem Data Transfer Object
 * Objeto de Transferencia de Datos para Items de Pedido
 * 
 * DTO usado para transferir información de items de pedidos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {

    private Long id;

    @NotNull(message = "El ID de producto es requerido")
    @Positive(message = "El ID de producto debe ser positivo")
    private Long productId;

    @NotBlank(message = "El nombre del producto es requerido")
    @Size(max = 200, message = "El nombre del producto no puede exceder 200 caracteres")
    private String productName;

    @NotNull(message = "La cantidad es requerida")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Max(value = 1000, message = "La cantidad no puede exceder 1000")
    private Integer quantity;

    @NotNull(message = "El precio unitario es requerido")
    @DecimalMin(value = "0.01", message = "El precio unitario debe ser mayor a 0")
    private BigDecimal unitPrice;

    private BigDecimal subtotal;
}
