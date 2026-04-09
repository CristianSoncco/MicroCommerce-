package com.microcommerce.user.controller;

import com.microcommerce.user.dto.UserDTO;
import com.microcommerce.user.dto.response.ApiResponse;
import com.microcommerce.user.dto.response.UserResponse;
import com.microcommerce.user.entity.Role;
import com.microcommerce.user.entity.User;
import com.microcommerce.user.mapper.UserMapper;
import com.microcommerce.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for User CRUD and query operations (protected endpoints)
 * Controlador REST para operaciones CRUD y consultas de usuario (endpoints protegidos)
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "API de gestion de usuarios | User management API")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    // ==================== CRUD Operations ====================

    /**
     * Get all users
     * Obtener todos los usuarios
     */
    @GetMapping
    @Operation(
        summary = "Listar todos los usuarios | List all users",
        description = "Obtiene la lista de todos los usuarios registrados | Gets the list of all registered users"
    )
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {

        log.info("Solicitud para obtener todos los usuarios");

        List<User> users = userService.getAllUsers();
        List<UserResponse> responses = users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get user by ID
     * Obtener usuario por ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener usuario por ID | Get user by ID",
        description = "Obtiene un usuario por su ID | Gets a user by their ID"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Usuario encontrado | User found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado | User not found"
        )
    })
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "ID del usuario | User ID")
            @PathVariable Long id) {

        log.info("Solicitud para obtener usuario con ID: {}", id);

        User user = userService.getUserById(id);
        UserResponse response = userMapper.toResponse(user);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get user by email
     * Obtener usuario por email
     */
    @GetMapping("/email/{email}")
    @Operation(
        summary = "Obtener usuario por email | Get user by email",
        description = "Obtiene un usuario por su email | Gets a user by their email"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Usuario encontrado | User found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado | User not found"
        )
    })
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(
            @Parameter(description = "Email del usuario | User email")
            @PathVariable String email) {

        log.info("Solicitud para obtener usuario con email: {}", email);

        User user = userService.getUserByEmail(email);
        UserResponse response = userMapper.toResponse(user);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update user profile
     * Actualizar perfil de usuario
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar usuario | Update user",
        description = "Actualiza el perfil de un usuario existente | Updates an existing user's profile"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Usuario actualizado | User updated"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado | User not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "El email ya esta en uso | Email already in use"
        )
    })
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @Parameter(description = "ID del usuario | User ID")
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {

        log.info("Solicitud para actualizar usuario con ID: {}", id);

        User user = userService.updateUser(id, userDTO);
        UserResponse response = userMapper.toResponse(user);

        return ResponseEntity.ok(ApiResponse.success("Usuario actualizado exitosamente", response));
    }

    /**
     * Deactivate user (soft delete)
     * Desactivar usuario (eliminacion logica)
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Desactivar usuario | Deactivate user",
        description = "Desactiva un usuario (eliminacion logica) | Deactivates a user (soft delete)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Usuario desactivado | User deactivated"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado | User not found"
        )
    })
    public ResponseEntity<ApiResponse<Void>> deactivateUser(
            @Parameter(description = "ID del usuario | User ID")
            @PathVariable Long id) {

        log.info("Solicitud para desactivar usuario con ID: {}", id);

        userService.deactivateUser(id);

        return ResponseEntity.ok(ApiResponse.success("Usuario desactivado exitosamente", null));
    }

    // ==================== Query Operations ====================

    /**
     * Get active users
     * Obtener usuarios activos
     */
    @GetMapping("/active")
    @Operation(
        summary = "Listar usuarios activos | List active users",
        description = "Obtiene la lista de usuarios activos | Gets the list of active users"
    )
    public ResponseEntity<ApiResponse<List<UserResponse>>> getActiveUsers() {

        log.info("Solicitud para obtener usuarios activos");

        List<User> users = userService.getActiveUsers();
        List<UserResponse> responses = users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Search users by name
     * Buscar usuarios por nombre
     */
    @GetMapping("/search")
    @Operation(
        summary = "Buscar usuarios por nombre | Search users by name",
        description = "Busca usuarios que contengan el texto en su nombre | Searches for users containing the text in their name"
    )
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchByName(
            @Parameter(description = "Texto a buscar | Text to search")
            @RequestParam String name) {

        log.info("Solicitud para buscar usuarios por nombre: {}", name);

        List<User> users = userService.searchByName(name);
        List<UserResponse> responses = users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get users by role
     * Obtener usuarios por rol
     */
    @GetMapping("/role/{role}")
    @Operation(
        summary = "Obtener usuarios por rol | Get users by role",
        description = "Obtiene usuarios filtrados por rol | Gets users filtered by role"
    )
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(
            @Parameter(description = "Rol del usuario (CUSTOMER, ADMIN) | User role (CUSTOMER, ADMIN)")
            @PathVariable Role role) {

        log.info("Solicitud para obtener usuarios por rol: {}", role);

        List<User> users = userService.getUsersByRole(role);
        List<UserResponse> responses = users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get active users by role
     * Obtener usuarios activos por rol
     */
    @GetMapping("/role/{role}/active")
    @Operation(
        summary = "Obtener usuarios activos por rol | Get active users by role",
        description = "Obtiene usuarios activos filtrados por rol | Gets active users filtered by role"
    )
    public ResponseEntity<ApiResponse<List<UserResponse>>> getActiveUsersByRole(
            @Parameter(description = "Rol del usuario (CUSTOMER, ADMIN) | User role (CUSTOMER, ADMIN)")
            @PathVariable Role role) {

        log.info("Solicitud para obtener usuarios activos por rol: {}", role);

        List<User> users = userService.getActiveUsersByRole(role);
        List<UserResponse> responses = users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Count users by role
     * Contar usuarios por rol
     */
    @GetMapping("/count/role/{role}")
    @Operation(
        summary = "Contar usuarios por rol | Count users by role",
        description = "Cuenta el numero de usuarios con un rol especifico | Counts the number of users with a specific role"
    )
    public ResponseEntity<ApiResponse<Long>> countByRole(
            @Parameter(description = "Rol del usuario (CUSTOMER, ADMIN) | User role (CUSTOMER, ADMIN)")
            @PathVariable Role role) {

        log.info("Solicitud para contar usuarios por rol: {}", role);

        long count = userService.countByRole(role);

        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * Count active users
     * Contar usuarios activos
     */
    @GetMapping("/count/active")
    @Operation(
        summary = "Contar usuarios activos | Count active users",
        description = "Cuenta el numero de usuarios activos | Counts the number of active users"
    )
    public ResponseEntity<ApiResponse<Long>> countActiveUsers() {

        log.info("Solicitud para contar usuarios activos");

        long count = userService.countActiveUsers();

        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
