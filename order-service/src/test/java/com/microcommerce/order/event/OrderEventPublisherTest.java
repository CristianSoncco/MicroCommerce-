package com.microcommerce.order.event;

import com.microcommerce.order.config.RabbitMQConfig;
import com.microcommerce.order.entity.Order;
import com.microcommerce.order.entity.Order.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for OrderEventPublisher.
 * Tests unitarios para OrderEventPublisher.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderEventPublisher Tests")
class OrderEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderEventPublisher publisher;

    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .id("order-001")
                .userId(1L)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("150.50"))
                .build();
    }

    @Test
    @DisplayName("publishOrderCreated - debe enviar evento al exchange con routing key correcta")
    void publishOrderCreated_sendsEventWithCorrectRoutingKey() {
        publisher.publishOrderCreated(order);

        ArgumentCaptor<OrderEvent> captor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.ORDERS_EXCHANGE),
                eq(RabbitMQConfig.ORDER_CREATED_ROUTING_KEY),
                captor.capture());

        OrderEvent event = captor.getValue();
        assertThat(event.getEventType()).isEqualTo(OrderEventPublisher.EVENT_ORDER_CREATED);
        assertThat(event.getOrderId()).isEqualTo("order-001");
        assertThat(event.getUserId()).isEqualTo(1L);
        assertThat(event.getStatus()).isEqualTo("PENDING");
        assertThat(event.getTotalAmount()).isEqualByComparingTo(new BigDecimal("150.50"));
        assertThat(event.getEventId()).isNotBlank();
        assertThat(event.getOccurredAt()).isNotNull();
    }

    @Test
    @DisplayName("publishOrderCancelled - debe enviar evento con routing key de cancelacion")
    void publishOrderCancelled_sendsCancelledEvent() {
        order.setStatus(OrderStatus.CANCELLED);

        publisher.publishOrderCancelled(order);

        ArgumentCaptor<OrderEvent> captor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.ORDERS_EXCHANGE),
                eq(RabbitMQConfig.ORDER_CANCELLED_ROUTING_KEY),
                captor.capture());

        assertThat(captor.getValue().getEventType()).isEqualTo(OrderEventPublisher.EVENT_ORDER_CANCELLED);
        assertThat(captor.getValue().getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("publishOrderCreated - debe capturar AmqpException sin propagarla")
    void publishOrderCreated_swallowsAmqpException() {
        doThrow(new AmqpException("broker down"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(OrderEvent.class));

        // No exception should propagate
        publisher.publishOrderCreated(order);

        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(OrderEvent.class));
    }
}
