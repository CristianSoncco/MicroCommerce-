package com.microcommerce.order.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Incoming payment event consumed by the Order Service.
 * Evento de pago entrante consumido por el Order Service.
 *
 * Mirror of the payload published by the Payment Service.
 * Refleja el payload publicado por el Payment Service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique event identifier. / Identificador unico del evento. */
    private String eventId;

    /** Event type (e.g. PAYMENT_COMPLETED, PAYMENT_FAILED). / Tipo de evento. */
    private String eventType;

    /** Payment identifier. / Identificador del pago. */
    private Long paymentId;

    /** Related order identifier. / Identificador de la orden relacionada. */
    private String orderId;

    /** User identifier. / Identificador del usuario. */
    private Long userId;

    /** Payment status. / Estado del pago. */
    private String status;

    /** Payment amount. / Monto del pago. */
    private BigDecimal amount;

    /** Currency code. / Codigo de moneda. */
    private String currency;

    /** Failure reason if the payment failed. / Motivo del fallo si el pago fallo. */
    private String failureReason;

    /** Event timestamp. / Marca temporal del evento. */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime occurredAt;
}
