package com.microcommerce.order.event;

import com.microcommerce.order.entity.Order.OrderStatus;
import com.microcommerce.order.exception.InvalidOrderStatusException;
import com.microcommerce.order.exception.OrderNotFoundException;
import com.microcommerce.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PaymentEventListener.
 * Tests unitarios para PaymentEventListener.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentEventListener Tests")
class PaymentEventListenerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentEventListener listener;

    @Test
    @DisplayName("onPaymentEvent - PAYMENT_COMPLETED debe actualizar pedido a PAID")
    void onPaymentEvent_paymentCompleted_updatesOrderToPaid() {
        PaymentEvent event = PaymentEvent.builder()
                .eventId("evt-1")
                .eventType(PaymentEventListener.EVENT_PAYMENT_COMPLETED)
                .orderId("order-001")
                .paymentId(10L)
                .build();
        when(orderService.updateOrderStatus("order-001", OrderStatus.PAID)).thenReturn(null);

        listener.onPaymentEvent(event);

        verify(orderService).updateOrderStatus(eq("order-001"), eq(OrderStatus.PAID));
    }

    @Test
    @DisplayName("onPaymentEvent - PAYMENT_FAILED no debe actualizar estado del pedido")
    void onPaymentEvent_paymentFailed_doesNotUpdateStatus() {
        PaymentEvent event = PaymentEvent.builder()
                .eventId("evt-2")
                .eventType(PaymentEventListener.EVENT_PAYMENT_FAILED)
                .orderId("order-002")
                .failureReason("Tarjeta rechazada")
                .build();

        listener.onPaymentEvent(event);

        verify(orderService, never()).updateOrderStatus(eq("order-002"), eq(OrderStatus.PAID));
    }

    @Test
    @DisplayName("onPaymentEvent - evento null debe ser descartado sin efectos")
    void onPaymentEvent_nullEvent_isIgnored() {
        listener.onPaymentEvent(null);

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("onPaymentEvent - tipo desconocido debe ser descartado")
    void onPaymentEvent_unknownType_isIgnored() {
        PaymentEvent event = PaymentEvent.builder()
                .eventId("evt-3")
                .eventType("UNKNOWN")
                .orderId("order-003")
                .build();

        listener.onPaymentEvent(event);

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("onPaymentEvent - OrderNotFoundException se maneja silenciosamente")
    void onPaymentEvent_orderNotFound_isHandled() {
        PaymentEvent event = PaymentEvent.builder()
                .eventId("evt-4")
                .eventType(PaymentEventListener.EVENT_PAYMENT_COMPLETED)
                .orderId("missing")
                .build();
        when(orderService.updateOrderStatus("missing", OrderStatus.PAID))
                .thenThrow(new OrderNotFoundException("missing"));

        listener.onPaymentEvent(event);

        verify(orderService).updateOrderStatus(eq("missing"), eq(OrderStatus.PAID));
    }

    @Test
    @DisplayName("onPaymentEvent - InvalidOrderStatusException se maneja silenciosamente")
    void onPaymentEvent_invalidStatus_isHandled() {
        PaymentEvent event = PaymentEvent.builder()
                .eventId("evt-5")
                .eventType(PaymentEventListener.EVENT_PAYMENT_COMPLETED)
                .orderId("order-005")
                .build();
        when(orderService.updateOrderStatus("order-005", OrderStatus.PAID))
                .thenThrow(new InvalidOrderStatusException(OrderStatus.CANCELLED, OrderStatus.PAID));

        listener.onPaymentEvent(event);

        verify(orderService).updateOrderStatus(eq("order-005"), eq(OrderStatus.PAID));
    }
}
