package com.microcommerce.payment.event;

import com.microcommerce.payment.config.RabbitMQConfig;
import com.microcommerce.payment.entity.Payment;
import com.microcommerce.payment.entity.PaymentMethod;
import com.microcommerce.payment.entity.PaymentStatus;
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
 * Unit tests for PaymentEventPublisher.
 * Tests unitarios para PaymentEventPublisher.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentEventPublisher Tests")
class PaymentEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PaymentEventPublisher publisher;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder()
                .id(42L)
                .orderId(100L)
                .userId(7L)
                .amount(new BigDecimal("25.00"))
                .currency("USD")
                .status(PaymentStatus.COMPLETED)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();
    }

    @Test
    @DisplayName("publishPaymentCompleted - debe publicar evento con routing key completed")
    void publishPaymentCompleted_sendsCompletedEvent() {
        publisher.publishPaymentCompleted(payment);

        ArgumentCaptor<PaymentEvent> captor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.PAYMENTS_EXCHANGE),
                eq(RabbitMQConfig.PAYMENT_COMPLETED_ROUTING_KEY),
                captor.capture());

        PaymentEvent event = captor.getValue();
        assertThat(event.getEventType()).isEqualTo(PaymentEventPublisher.EVENT_PAYMENT_COMPLETED);
        assertThat(event.getPaymentId()).isEqualTo(42L);
        assertThat(event.getOrderId()).isEqualTo("100");
        assertThat(event.getUserId()).isEqualTo(7L);
        assertThat(event.getStatus()).isEqualTo("COMPLETED");
        assertThat(event.getAmount()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(event.getCurrency()).isEqualTo("USD");
        assertThat(event.getEventId()).isNotBlank();
        assertThat(event.getOccurredAt()).isNotNull();
    }

    @Test
    @DisplayName("publishPaymentFailed - debe publicar evento con routing key failed")
    void publishPaymentFailed_sendsFailedEvent() {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason("Tarjeta rechazada");

        publisher.publishPaymentFailed(payment);

        ArgumentCaptor<PaymentEvent> captor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.PAYMENTS_EXCHANGE),
                eq(RabbitMQConfig.PAYMENT_FAILED_ROUTING_KEY),
                captor.capture());

        PaymentEvent event = captor.getValue();
        assertThat(event.getEventType()).isEqualTo(PaymentEventPublisher.EVENT_PAYMENT_FAILED);
        assertThat(event.getFailureReason()).isEqualTo("Tarjeta rechazada");
    }

    @Test
    @DisplayName("publishPaymentRefunded - debe publicar evento con routing key refunded")
    void publishPaymentRefunded_sendsRefundedEvent() {
        payment.setStatus(PaymentStatus.REFUNDED);

        publisher.publishPaymentRefunded(payment);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.PAYMENTS_EXCHANGE),
                eq(RabbitMQConfig.PAYMENT_REFUNDED_ROUTING_KEY),
                any(PaymentEvent.class));
    }

    @Test
    @DisplayName("publishPaymentCompleted - debe capturar AmqpException sin propagarla")
    void publishPaymentCompleted_swallowsAmqpException() {
        doThrow(new AmqpException("broker down"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(PaymentEvent.class));

        publisher.publishPaymentCompleted(payment);

        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(PaymentEvent.class));
    }
}
