package com.microcommerce.user.exception;

/**
 * Exception thrown when attempting to create a user that already exists
 * Excepcion lanzada cuando se intenta crear un usuario que ya existe
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String email) {
        super("El usuario ya existe con el email: " + email);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
