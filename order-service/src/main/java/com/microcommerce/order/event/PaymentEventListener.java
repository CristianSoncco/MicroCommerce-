package com.microcommerce.order.event;

import com.microcommerce.order.config.RabbitMQConfig;
import com.microcommerce.order.entity.Order.OrderStatus;
import com.microcommerce.order.exception.InvalidOrderStatusException;
import com.microcommerce.order.exception.OrderNotFoundException;
import com.microcommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listens for payment events published by the Payment Service and updates
 * the related order accordingly.
 * Escucha los eventos de pago publicados por el Payment Service y actualiza
 * la orden relacionada en consecuencia.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    public static final String EVENT_PAYMENT_COMPLETED = "PAYMENT_COMPLETED";
    public static final String EVENT_PAYMENT_FAILED = "PAYMENT_FAILED";

    private final OrderService orderService;

    /**
     * Handle incoming payment events.
     * Manejar eventos de pago entrantes.
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_PAYMENT_EVENTS_QUEUE)
    public void onPaymentEvent(PaymentEvent event) {
        if (event == null || event.getEventType() == null) {
            log.warn("Evento de pago recibido invalido, se descarta");
            return;
        }

        log.info("Evento de pago recibido. tipo={} paymentId={} orderId={}",
                event.getEventType(), event.getPaymentId(), event.getOrderId());

        switch (event.getEventType()) {
            case EVENT_PAYMENT_COMPLETED -> handlePaymentCompleted(event);
            case EVENT_PAYMENT_FAILED -> handlePaymentFailed(event);
            default -> log.debug("Tipo de evento de pago no manejado: {}", event.getEventType());
        }
    }

    private void handlePaymentCompleted(PaymentEvent event) {
        if (event.getOrderId() == null) {
            log.warn("Evento PAYMENT_COMPLETED sin orderId, se descarta");
            return;
        }
        try {
            orderService.updateOrderStatus(event.getOrderId(), OrderStatus.PAID);
            log.info("Pedido {} marcado como PAID tras evento de pago {}",
                    event.getOrderId(), event.getEventId());
        } catch (OrderNotFoundException ex) {
            log.warn("Pedido {} no encontrado al procesar PAYMENT_COMPLETED", event.getOrderId());
        } catch (InvalidOrderStatusException ex) {
            log.warn("Transicion invalida al marcar pedido {} como PAID: {}",
                    event.getOrderId(), ex.getMessage());
        }
    }

    private void handlePaymentFailed(PaymentEvent event) {
        if (event.getOrderId() == null) {
            log.warn("Evento PAYMENT_FAILED sin orderId, se descarta");
            return;
        }
        log.warn("Pago fallido recibido para pedido {}. Motivo: {}",
                event.getOrderId(), event.getFailureReason());
    }
}
