package com.microcommerce.order.dto;

import com.microcommerce.order.entity.Order.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Data Transfer Object
 * Objeto de Transferencia de Datos para Pedidos
 *
 * DTO used for transferring order information between layers and REST APIs.
 * DTO usado para transferir informacion de pedidos entre capas y APIs REST.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    private String id;

    @NotNull(message = "El ID de usuario es requerido")
    @Positive(message = "El ID de usuario debe ser positivo")
    private Long userId;

    private OrderStatus status;

    private BigDecimal totalAmount;

    @NotBlank(message = "La direccion de envio es requerida")
    @Size(max = 500, message = "La direccion de envio no puede exceder 500 caracteres")
    private String shippingAddress;

    @Size(max = 50, message = "El metodo de pago no puede exceder 50 caracteres")
    private String paymentMethod;

    private String transactionId;

    @Size(max = 1000, message = "Las notas no pueden exceder 1000 caracteres")
    private String notes;

    @NotEmpty(message = "El pedido debe contener al menos un item")
    @Valid
    private List<OrderItemDTO> items;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime shippedAt;

    private LocalDateTime deliveredAt;
}
