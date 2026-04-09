package com.microcommerce.user.exception;

/**
 * Exception thrown when a disabled user attempts to authenticate
 * Excepcion lanzada cuando un usuario deshabilitado intenta autenticarse
 */
public class UserDisabledException extends RuntimeException {

    public UserDisabledException(String email) {
        super("La cuenta del usuario esta deshabilitada: " + email);
    }
}
