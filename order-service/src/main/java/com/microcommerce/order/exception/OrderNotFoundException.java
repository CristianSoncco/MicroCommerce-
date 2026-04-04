package com.microcommerce.order.exception;

/**
 * Exception thrown when an order is not found
 * Excepcion lanzada cuando un pedido no se encuentra
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String id) {
        super("Pedido no encontrado con id: " + id);
    }
}
