package com.microcommerce.product.exception;

/**
 * Exception thrown when there is insufficient stock
 * Excepción lanzada cuando hay stock insuficiente
 */
public class InsufficientStockException extends RuntimeException {
    
    public InsufficientStockException(Long productId) {
        super("Stock insuficiente para el producto con id: " + productId);
    }
    
    public InsufficientStockException(Long productId, Integer requested, Integer available) {
        super(String.format("Stock insuficiente para el producto %d. Solicitado: %d, Disponible: %d", 
            productId, requested, available));
    }
    
    public InsufficientStockException(String message) {
        super(message);
    }
}
