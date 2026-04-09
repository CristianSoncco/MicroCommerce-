package com.microcommerce.user.controller;

import com.microcommerce.user.dto.request.LoginRequest;
import com.microcommerce.user.dto.request.RegisterRequest;
import com.microcommerce.user.dto.response.ApiResponse;
import com.microcommerce.user.dto.response.AuthResponse;
import com.microcommerce.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication operations (public endpoints)
 * Controlador REST para operaciones de autenticacion (endpoints publicos)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "API de autenticacion | Authentication API")
public class AuthController {

    private final UserService userService;

    /**
     * Register a new user
     * Registrar un nuevo usuario
     */
    @PostMapping("/register")
    @Operation(
        summary = "Registrar usuario | Register user",
        description = "Registra un nuevo usuario y devuelve un token JWT | Registers a new user and returns a JWT token"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Usuario registrado exitosamente | User registered successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Datos de entrada invalidos | Invalid input data"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "El usuario ya existe | User already exists"
        )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Solicitud de registro para: {}", request.getEmail());

        AuthResponse authResponse = userService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usuario registrado exitosamente", authResponse));
    }

    /**
     * Authenticate user and return JWT token
     * Autenticar usuario y devolver token JWT
     */
    @PostMapping("/login")
    @Operation(
        summary = "Iniciar sesion | Login",
        description = "Autentica al usuario y devuelve un token JWT | Authenticates the user and returns a JWT token"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Inicio de sesion exitoso | Login successful"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Credenciales invalidas | Invalid credentials"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Usuario deshabilitado | User disabled"
        )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Solicitud de inicio de sesion para: {}", request.getEmail());

        AuthResponse authResponse = userService.login(request);

        return ResponseEntity.ok(ApiResponse.success("Inicio de sesion exitoso", authResponse));
    }
}
