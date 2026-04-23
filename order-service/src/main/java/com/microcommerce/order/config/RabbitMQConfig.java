package com.microcommerce.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for the Order Service.
 * Configuracion de RabbitMQ para el Order Service.
 *
 * Declares the orders topic exchange and the payment events queue so the
 * service can publish order events and consume payment events.
 *
 * Declara el topic exchange de ordenes y la cola de eventos de pagos para que
 * el servicio pueda publicar eventos de pedido y consumir eventos de pago.
 */
@Configuration
public class RabbitMQConfig {

    public static final String ORDERS_EXCHANGE = "orders.exchange";
    public static final String PAYMENTS_EXCHANGE = "payments.exchange";

    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String ORDER_CANCELLED_ROUTING_KEY = "order.cancelled";
    public static final String PAYMENT_EVENTS_ROUTING_KEY = "payment.#";

    public static final String ORDER_PAYMENT_EVENTS_QUEUE = "order.payment-events.queue";
    public static final String ORDER_PAYMENT_EVENTS_DLQ = "order.payment-events.dlq";
    public static final String ORDER_PAYMENT_EVENTS_DLX = "order.payment-events.dlx";

    /**
     * Orders topic exchange where the service publishes its events.
     * Topic exchange de ordenes donde el servicio publica sus eventos.
     */
    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange(ORDERS_EXCHANGE, true, false);
    }

    /**
     * Payments topic exchange (declared here so the consumer can bind without
     * depending on the Payment Service startup order).
     * Topic exchange de pagos (declarado aqui para que el consumidor pueda
     * asociarse sin depender del arranque del Payment Service).
     */
    @Bean
    public TopicExchange paymentsExchange() {
        return new TopicExchange(PAYMENTS_EXCHANGE, true, false);
    }

    /**
     * Dead letter exchange for rejected payment events.
     * Exchange de dead letter para eventos de pago rechazados.
     */
    @Bean
    public TopicExchange paymentEventsDeadLetterExchange() {
        return new TopicExchange(ORDER_PAYMENT_EVENTS_DLX, true, false);
    }

    /**
     * Dead letter queue for payment events.
     * Cola de dead letter para eventos de pago.
     */
    @Bean
    public Queue paymentEventsDeadLetterQueue() {
        return QueueBuilder.durable(ORDER_PAYMENT_EVENTS_DLQ).build();
    }

    @Bean
    public Binding paymentEventsDeadLetterBinding() {
        return BindingBuilder.bind(paymentEventsDeadLetterQueue())
                .to(paymentEventsDeadLetterExchange())
                .with("#");
    }

    /**
     * Queue consumed by this service to receive payment events.
     * Cola consumida por este servicio para recibir eventos de pago.
     */
    @Bean
    public Queue paymentEventsQueue() {
        return QueueBuilder.durable(ORDER_PAYMENT_EVENTS_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_PAYMENT_EVENTS_DLX)
                .build();
    }

    @Bean
    public Binding paymentEventsBinding() {
        return BindingBuilder.bind(paymentEventsQueue())
                .to(paymentsExchange())
                .with(PAYMENT_EVENTS_ROUTING_KEY);
    }

    /**
     * Jackson JSON message converter for AMQP.
     * Convertidor de mensajes JSON (Jackson) para AMQP.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate configured with the JSON converter.
     * RabbitTemplate configurado con el convertidor JSON.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
