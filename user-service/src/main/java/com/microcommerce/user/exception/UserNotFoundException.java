package com.microcommerce.user.exception;

/**
 * Exception thrown when a user is not found
 * Excepcion lanzada cuando un usuario no se encuentra
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("Usuario no encontrado con id: " + id);
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
