package com.microcommerce.payment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for refunding a payment
 * DTO de solicitud para reembolsar un pago
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequest {

    @DecimalMin(value = "0.01", message = "El monto de reembolso debe ser mayor a cero")
    private BigDecimal amount;

    @Size(max = 500, message = "La razon no puede exceder 500 caracteres")
    private String reason;
}
