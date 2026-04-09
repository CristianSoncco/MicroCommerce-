package com.microcommerce.user.service;

import com.microcommerce.user.dto.UserDTO;
import com.microcommerce.user.dto.request.LoginRequest;
import com.microcommerce.user.dto.request.RegisterRequest;
import com.microcommerce.user.dto.response.AuthResponse;
import com.microcommerce.user.entity.Role;
import com.microcommerce.user.entity.User;

import java.util.List;

/**
 * Service interface for User operations and authentication
 * Interfaz de servicio para operaciones de usuario y autenticacion
 */
public interface UserService {

    // ==================== Authentication ====================

    /**
     * Register a new user
     * Registrar un nuevo usuario
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticate user and return JWT token
     * Autenticar usuario y devolver token JWT
     */
    AuthResponse login(LoginRequest request);

    // ==================== CRUD Operations ====================

    /**
     * Get user by ID
     * Obtener usuario por ID
     */
    User getUserById(Long id);

    /**
     * Get user by email
     * Obtener usuario por email
     */
    User getUserByEmail(String email);

    /**
     * Get all users
     * Obtener todos los usuarios
     */
    List<User> getAllUsers();

    /**
     * Get all active users
     * Obtener todos los usuarios activos
     */
    List<User> getActiveUsers();

    /**
     * Update user profile
     * Actualizar perfil de usuario
     */
    User updateUser(Long id, UserDTO dto);

    /**
     * Deactivate user (soft delete)
     * Desactivar usuario (eliminacion logica)
     */
    void deactivateUser(Long id);

    // ==================== Query Operations ====================

    /**
     * Search users by name
     * Buscar usuarios por nombre
     */
    List<User> searchByName(String term);

    /**
     * Get users by role
     * Obtener usuarios por rol
     */
    List<User> getUsersByRole(Role role);

    /**
     * Get active users by role
     * Obtener usuarios activos por rol
     */
    List<User> getActiveUsersByRole(Role role);

    /**
     * Count users by role
     * Contar usuarios por rol
     */
    long countByRole(Role role);

    /**
     * Count active users
     * Contar usuarios activos
     */
    long countActiveUsers();
}
