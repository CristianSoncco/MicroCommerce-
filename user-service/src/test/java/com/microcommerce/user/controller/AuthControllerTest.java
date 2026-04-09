package com.microcommerce.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microcommerce.user.config.SecurityConfig;
import com.microcommerce.user.dto.request.LoginRequest;
import com.microcommerce.user.dto.request.RegisterRequest;
import com.microcommerce.user.dto.response.AuthResponse;
import com.microcommerce.user.exception.InvalidCredentialsException;
import com.microcommerce.user.exception.UserAlreadyExistsException;
import com.microcommerce.user.exception.UserDisabledException;
import com.microcommerce.user.exception.handler.GlobalExceptionHandler;
import com.microcommerce.user.security.JwtAuthenticationEntryPoint;
import com.microcommerce.user.security.JwtAuthenticationFilter;
import com.microcommerce.user.security.JwtTokenProvider;
import com.microcommerce.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests para AuthController con MockMvc
 * Controller tests for AuthController with MockMvc
 */
@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class})
@ActiveProfiles("test-nodb")
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "newuser@example.com",
                "password123",
                "New",
                "User",
                "+5511999999999",
                "100 New Street"
        );

        loginRequest = new LoginRequest("john.doe@example.com", "password123");

        authResponse = AuthResponse.of(
                "mock-jwt-token", 1L, "john.doe@example.com", "CUSTOMER"
        );
    }

    // ==================== Register Tests ====================

    @Test
    @DisplayName("POST /api/auth/register - request valido - debe retornar 201")
    void register_ValidRequest_Returns201() throws Exception {
        // Given
        when(userService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuario registrado exitosamente"))
                .andExpect(jsonPath("$.data.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.data.type").value("Bearer"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));

        verify(userService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - email duplicado - debe retornar 409")
    void register_DuplicateEmail_Returns409() throws Exception {
        // Given
        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("newuser@example.com"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register - sin email - debe retornar 400")
    void register_MissingEmail_Returns400() throws Exception {
        // Given
        registerRequest.setEmail(null);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(userService, never()).register(any());
    }

    @Test
    @DisplayName("POST /api/auth/register - sin contrasena - debe retornar 400")
    void register_MissingPassword_Returns400() throws Exception {
        // Given
        registerRequest.setPassword(null);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(userService, never()).register(any());
    }

    @Test
    @DisplayName("POST /api/auth/register - sin nombre - debe retornar 400")
    void register_MissingFirstName_Returns400() throws Exception {
        // Given
        registerRequest.setFirstName(null);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(userService, never()).register(any());
    }

    @Test
    @DisplayName("POST /api/auth/register - email invalido - debe retornar 400")
    void register_InvalidEmail_Returns400() throws Exception {
        // Given
        registerRequest.setEmail("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(userService, never()).register(any());
    }

    @Test
    @DisplayName("POST /api/auth/register - contrasena muy corta - debe retornar 400")
    void register_ShortPassword_Returns400() throws Exception {
        // Given
        registerRequest.setPassword("short");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(userService, never()).register(any());
    }

    // ==================== Login Tests ====================

    @Test
    @DisplayName("POST /api/auth/login - credenciales validas - debe retornar 200")
    void login_ValidCredentials_Returns200() throws Exception {
        // Given
        when(userService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Inicio de sesion exitoso"))
                .andExpect(jsonPath("$.data.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.data.userId").value(1));

        verify(userService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - credenciales invalidas - debe retornar 401")
    void login_InvalidCredentials_Returns401() throws Exception {
        // Given
        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException());

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /api/auth/login - usuario deshabilitado - debe retornar 403")
    void login_DisabledUser_Returns403() throws Exception {
        // Given
        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new UserDisabledException("john.doe@example.com"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("POST /api/auth/login - sin email - debe retornar 400")
    void login_MissingEmail_Returns400() throws Exception {
        // Given
        loginRequest.setEmail(null);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(userService, never()).login(any());
    }

    @Test
    @DisplayName("POST /api/auth/login - sin contrasena - debe retornar 400")
    void login_MissingPassword_Returns400() throws Exception {
        // Given
        loginRequest.setPassword(null);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(userService, never()).login(any());
    }
}
