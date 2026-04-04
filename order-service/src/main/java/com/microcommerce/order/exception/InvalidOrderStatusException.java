package com.microcommerce.order.exception;

import com.microcommerce.order.entity.Order.OrderStatus;

/**
 * Exception thrown when an order status transition is invalid
 * Excepcion lanzada cuando una transicion de estado de pedido es invalida
 */
public class InvalidOrderStatusException extends RuntimeException {

    public InvalidOrderStatusException(OrderStatus currentStatus, OrderStatus targetStatus) {
        super("Transicion de estado invalida de " + currentStatus + " a " + targetStatus);
    }
}
