package com.microcommerce.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.Base64;

/**
 * Validates JWT tokens at the gateway level before protected routes are forwarded.
 * Valida tokens JWT en el gateway antes de reenviar rutas protegidas.
 */
@Component
public class JwtTokenValidator {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenValidator.class);

    private final SecretKey secretKey;

    public JwtTokenValidator(@Value("${jwt.secret}") String secret) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT expirado en gateway: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("JWT malformado en gateway: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("JWT no soportado en gateway: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT vacio o nulo en gateway: {}", ex.getMessage());
        } catch (JwtException ex) {
            log.warn("JWT invalido en gateway: {}", ex.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
