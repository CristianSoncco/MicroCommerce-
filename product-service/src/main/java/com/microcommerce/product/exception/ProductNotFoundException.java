package com.microcommerce.product.exception;

/**
 * Exception thrown when a product is not found
 * Excepción lanzada cuando un producto no se encuentra
 */
public class ProductNotFoundException extends RuntimeException {
    
    public ProductNotFoundException(Long id) {
        super("Product not found with id: " + id);
    }
    
    public ProductNotFoundException(String message) {
        super(message);
    }
}
