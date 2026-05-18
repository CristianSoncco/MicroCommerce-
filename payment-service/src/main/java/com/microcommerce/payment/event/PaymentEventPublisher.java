package com.microcommerce.payment.event;

import com.microcommerce.payment.config.RabbitMQConfig;
import com.microcommerce.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Publishes payment events to the payments exchange.
 * Publica eventos de pago al exchange de pagos.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    public static final String EVENT_PAYMENT_COMPLETED = "PAYMENT_COMPLETED";
    public static final String EVENT_PAYMENT_FAILED = "PAYMENT_FAILED";
    public static final String EVENT_PAYMENT_REFUNDED = "PAYMENT_REFUNDED";

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publish PAYMENT_COMPLETED event.
     * Publicar evento PAYMENT_COMPLETED.
     */
    public void publishPaymentCompleted(Payment payment) {
        PaymentEvent event = buildEvent(payment, EVENT_PAYMENT_COMPLETED);
        send(RabbitMQConfig.PAYMENT_COMPLETED_ROUTING_KEY, event);
    }

    /**
     * Publish PAYMENT_FAILED event.
     * Publicar evento PAYMENT_FAILED.
     */
    public void publishPaymentFailed(Payment payment) {
        PaymentEvent event = buildEvent(payment, EVENT_PAYMENT_FAILED);
        send(RabbitMQConfig.PAYMENT_FAILED_ROUTING_KEY, event);
    }

    /**
     * Publish PAYMENT_REFUNDED event.
     * Publicar evento PAYMENT_REFUNDED.
     */
    public void publishPaymentRefunded(Payment payment) {
        PaymentEvent event = buildEvent(payment, EVENT_PAYMENT_REFUNDED);
        send(RabbitMQConfig.PAYMENT_REFUNDED_ROUTING_KEY, event);
    }

    private PaymentEvent buildEvent(Payment payment, String eventType) {
        return PaymentEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .paymentId(payment.getId())
                .orderId(payment.getOrderId() != null ? String.valueOf(payment.getOrderId()) : null)
                .userId(payment.getUserId())
                .status(payment.getStatus() != null ? payment.getStatus().name() : null)
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .failureReason(payment.getFailureReason())
                .occurredAt(LocalDateTime.now())
                .build();
    }

    private void send(String routingKey, PaymentEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.PAYMENTS_EXCHANGE, routingKey, event);
            log.info("Evento de pago publicado. tipo={} routingKey={} paymentId={}",
                    event.getEventType(), routingKey, event.getPaymentId());
        } catch (AmqpException ex) {
            log.error("Error publicando evento de pago. tipo={} routingKey={} paymentId={} causa={}",
                    event.getEventType(), routingKey, event.getPaymentId(), ex.getMessage());
        }
    }
}
