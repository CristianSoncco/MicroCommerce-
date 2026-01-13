package com.microcommerce.product.exception;

/**
 * Exception thrown when attempting to create a product that already exists
 * Excepción lanzada cuando se intenta crear un producto que ya existe
 */
public class ProductAlreadyExistsException extends RuntimeException {
    
    public ProductAlreadyExistsException(String name) {
        super("El producto ya existe con el nombre: " + name);
    }
    
    public ProductAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
