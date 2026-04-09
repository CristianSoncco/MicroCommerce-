package com.microcommerce.user.exception;

/**
 * Exception thrown when authentication credentials are invalid
 * Excepcion lanzada cuando las credenciales de autenticacion son invalidas
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Credenciales invalidas");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
