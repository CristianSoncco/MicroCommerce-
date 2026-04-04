package com.microcommerce.order.exception;

/**
 * Exception thrown when trying to create an order without items
 * Excepcion lanzada cuando se intenta crear un pedido sin items
 */
public class EmptyOrderException extends RuntimeException {

    public EmptyOrderException() {
        super("El pedido debe contener al menos un item");
    }
}
