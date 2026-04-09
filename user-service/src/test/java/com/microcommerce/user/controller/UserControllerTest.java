package com.microcommerce.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microcommerce.user.config.SecurityConfig;
import com.microcommerce.user.dto.UserDTO;
import com.microcommerce.user.dto.response.UserResponse;
import com.microcommerce.user.entity.Role;
import com.microcommerce.user.entity.User;
import com.microcommerce.user.exception.UserAlreadyExistsException;
import com.microcommerce.user.exception.UserNotFoundException;
import com.microcommerce.user.exception.handler.GlobalExceptionHandler;
import com.microcommerce.user.mapper.UserMapper;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests para UserController con MockMvc
 * Controller tests for UserController with MockMvc
 */
@WebMvcTest(UserController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class})
@ActiveProfiles("test-nodb")
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private UserDTO userDTO;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("john.doe@example.com")
                .password("$2a$10$encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .phone("+1234567890")
                .address("123 Main St")
                .role(Role.CUSTOMER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userDTO = new UserDTO(
                "updated@example.com",
                "newpassword123",
                "Updated",
                "Name",
                "+5511888888888",
                "200 Updated Blvd"
        );

        userResponse = UserResponse.builder()
                .id(1L)
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .phone("+1234567890")
                .address("123 Main St")
                .role(Role.CUSTOMER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== CRUD Operations Tests ====================

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /api/users - debe retornar lista de usuarios")
    void getAllUsers_ReturnsUserList() throws Exception {
        // Given
        List<User> users = Arrays.asList(user, user);
        when(userService.getAllUsers()).thenReturn(users);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)));

        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("GET /api/users - sin autenticacion - debe retornar 401")
    void getAllUsers_Unauthorized_Returns401() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /api/users/{id} - ID existente - debe retornar 200")
    void getUserById_ExistingId_Returns200() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("John"));

        verify(userService).getUserById(1L);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /api/users/{id} - ID no existente - debe retornar 404")
    void getUserById_NonExistingId_Returns404() throws Exception {
        // Given
        when(userService.getUserById(999L))
                .thenThrow(new UserNotFoundException(999L));

        // When & Then
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /api/users/email/{email} - email existente - debe retornar 200")
    void getUserByEmail_ExistingEmail_Returns200() throws Exception {
        // Given
        when(userService.getUserByEmail("john.doe@example.com")).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/users/email/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));

        verify(userService).getUserByEmail("john.doe@example.com");
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /api/users/email/{email} - email no existente - debe retornar 404")
    void getUserByEmail_NonExistingEmail_Returns404() throws Exception {
        // Given
        when(userService.getUserByEmail("nonexistent@example.com"))
                .thenThrow(new UserNotFoundException("Usuario no encontrado con email: nonexistent@example.com"));

        // When & Then
        mockMvc.perform(get("/api/users/email/nonexistent@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("PUT /api/users/{id} - request valido - debe retornar 200")
    void updateUser_ValidRequest_Returns200() throws Exception {
        // Given
        UserResponse updatedResponse = UserResponse.builder()
                .id(1L)
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("Name")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        when(userService.updateUser(eq(1L), any(UserDTO.class))).thenReturn(user);
        when(userMapper.toResponse(any(User.class))).thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuario actualizado exitosamente"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(userService).updateUser(eq(1L), any(UserDTO.class));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("PUT /api/users/{id} - ID no existente - debe retornar 404")
    void updateUser_NonExistingId_Returns404() throws Exception {
        // Given
        when(userService.updateUser(eq(999L), any(UserDTO.class)))
                .thenThrow(new UserNotFoundException(999L));

        // When & Then
        mockMvc.perform(put("/api/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("PUT /api/users/{id} - email duplicado - debe retornar 409")
    void updateUser_DuplicateEmail_Returns409() throws Exception {
        // Given
        when(userService.updateUser(eq(1L), any(UserDTO.class)))
                .thenThrow(new UserAlreadyExistsException("updated@example.com"));

        // When & Then
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("DELETE /api/users/{id} - ID existente - debe retornar 200")
    void deactivateUser_ExistingId_Returns200() throws Exception {
        // Given
        doNothing().when(userService).deactivateUser(1L);

        // When & Then
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuario desactivado exitosamente"));

        verify(userService).deactivateUser(1L);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("DELETE /api/users/{id} - ID no existente - debe retornar 404")
    void deactivateUser_NonExistingId_Returns404() throws Exception {
        // Given
        doThrow(new UserNotFoundException(999L)).when(userService).deactivateUser(999L);

        // When & Then
        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    // ==================== Query Operations Tests ====================

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /api/users/active - debe retornar usuarios activos")
    void getActiveUsers_ReturnsActiveUsers() throws Exception {
        // Given
        List<User> activeUsers = Arrays.asList(user);
        when(userService.getActiveUsers()).thenReturn(activeUsers);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/users/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(userService).getActiveUsers();
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /api/users/search - debe buscar por nombre")
    void searchByName_ReturnsMatchingUsers() throws Exception {
        // Given
        List<User> users = Arrays.asList(user);
        when(userService.searchByName("Doe")).thenReturn(users);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/users/search")
                        .param("name", "Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(userService).searchByName("Doe");
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /api/users/role/{role} - debe retornar usuarios por rol")
    void getUsersByRole_ReturnsUsersByRole() throws Exception {
        // Given
        List<User> customers = Arrays.asList(user);
        when(userService.getUsersByRole(Role.CUSTOMER)).thenReturn(customers);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/users/role/CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(userService).getUsersByRole(Role.CUSTOMER);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /api/users/role/{role}/active - debe retornar usuarios activos por rol")
    void getActiveUsersByRole_ReturnsActiveUsersByRole() throws Exception {
        // Given
        List<User> activeCustomers = Arrays.asList(user);
        when(userService.getActiveUsersByRole(Role.CUSTOMER)).thenReturn(activeCustomers);
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/users/role/CUSTOMER/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(userService).getActiveUsersByRole(Role.CUSTOMER);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /api/users/count/role/{role} - debe retornar conteo por rol")
    void countByRole_ReturnsCount() throws Exception {
        // Given
        when(userService.countByRole(Role.CUSTOMER)).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/users/count/role/CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(5));

        verify(userService).countByRole(Role.CUSTOMER);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    @DisplayName("GET /api/users/count/active - debe retornar conteo de activos")
    void countActiveUsers_ReturnsCount() throws Exception {
        // Given
        when(userService.countActiveUsers()).thenReturn(10L);

        // When & Then
        mockMvc.perform(get("/api/users/count/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(10));

        verify(userService).countActiveUsers();
    }
}
