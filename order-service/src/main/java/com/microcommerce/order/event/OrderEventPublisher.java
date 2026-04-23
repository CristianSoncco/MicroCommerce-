package com.microcommerce.order.event;

import com.microcommerce.order.config.RabbitMQConfig;
import com.microcommerce.order.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Publishes order related events to the orders exchange.
 * Publica eventos relacionados con pedidos al exchange de ordenes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    public static final String EVENT_ORDER_CREATED = "ORDER_CREATED";
    public static final String EVENT_ORDER_CANCELLED = "ORDER_CANCELLED";

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publish the event raised when a new order is created.
     * Publica el evento generado cuando se crea un nuevo pedido.
     *
     * @param order persisted order / pedido persistido
     */
    public void publishOrderCreated(Order order) {
        OrderEvent event = buildEvent(order, EVENT_ORDER_CREATED);
        send(RabbitMQConfig.ORDER_CREATED_ROUTING_KEY, event);
    }

    /**
     * Publish the event raised when an order is cancelled.
     * Publica el evento generado cuando un pedido se cancela.
     *
     * @param order cancelled order / pedido cancelado
     */
    public void publishOrderCancelled(Order order) {
        OrderEvent event = buildEvent(order, EVENT_ORDER_CANCELLED);
        send(RabbitMQConfig.ORDER_CANCELLED_ROUTING_KEY, event);
    }

    private OrderEvent buildEvent(Order order, String eventType) {
        return OrderEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .orderId(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .totalAmount(order.getTotalAmount())
                .occurredAt(LocalDateTime.now())
                .build();
    }

    private void send(String routingKey, OrderEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.ORDERS_EXCHANGE, routingKey, event);
            log.info("Evento de pedido publicado. tipo={} routingKey={} orderId={}",
                    event.getEventType(), routingKey, event.getOrderId());
        } catch (AmqpException ex) {
            log.error("Error publicando evento de pedido. tipo={} routingKey={} orderId={} causa={}",
                    event.getEventType(), routingKey, event.getOrderId(), ex.getMessage());
        }
    }
}
