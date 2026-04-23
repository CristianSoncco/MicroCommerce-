package com.microcommerce.payment.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event emitted by the Payment Service when a payment changes state.
 * Evento emitido por el Payment Service cuando un pago cambia de estado.
 *
 * Serialized as JSON and routed through the payments exchange.
 * Se serializa como JSON y se enruta a traves del exchange de pagos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    /** Failure reason when the payment failed. / Motivo del fallo cuando el pago fallo. */
    private String failureReason;

    /** Event timestamp. / Marca temporal del evento. */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime occurredAt;
}
