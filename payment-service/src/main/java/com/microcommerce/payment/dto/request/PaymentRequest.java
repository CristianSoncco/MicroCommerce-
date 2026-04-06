package com.microcommerce.payment.dto.request;

import com.microcommerce.payment.entity.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a payment
 * DTO de solicitud para crear un pago
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    @NotNull(message = "El ID de la orden es obligatorio")
    private Long orderId;

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long userId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private BigDecimal amount;

    @Size(min = 3, max = 3, message = "La moneda debe tener exactamente 3 caracteres")
    @Builder.Default
    private String currency = "USD";

    @NotNull(message = "El metodo de pago es obligatorio")
    private PaymentMethod paymentMethod;

    @Size(max = 500, message = "La descripcion no puede exceder 500 caracteres")
    private String description;

    @NotNull(message = "El token de pago es obligatorio")
    private String paymentToken;
}
