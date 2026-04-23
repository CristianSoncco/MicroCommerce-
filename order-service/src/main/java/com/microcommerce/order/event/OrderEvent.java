package com.microcommerce.order.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Base event emitted by the Order Service when an order changes state.
 * Evento base emitido por el Order Service cuando un pedido cambia de estado.
 *
 * This payload is serialized to JSON and routed through the orders exchange.
 * Este payload se serializa a JSON y se enruta a traves del exchange de pedidos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique event identifier. / Identificador unico del evento. */
    private String eventId;

    /** Event type (e.g. ORDER_CREATED, ORDER_CANCELLED). / Tipo de evento. */
    private String eventType;

    /** Order identifier. / Identificador del pedido. */
    private String orderId;

    /** User that owns the order. / Usuario duenio del pedido. */
    private Long userId;

    /** Order status at the time of the event. / Estado del pedido en el momento del evento. */
    private String status;

    /** Total amount of the order. / Monto total del pedido. */
    private BigDecimal totalAmount;

    /** Event timestamp. / Marca temporal del evento. */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime occurredAt;
}
