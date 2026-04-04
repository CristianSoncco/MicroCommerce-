package com.microcommerce.user.repository;

import com.microcommerce.user.entity.Role;
import com.microcommerce.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity
 * Interfaz de repositorio para la entidad User
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by email
     * Buscar un usuario por email
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user exists by email
     * Verificar si existe un usuario por email
     */
    boolean existsByEmail(String email);

    /**
     * Find users by role
     * Buscar usuarios por rol
     */
    List<User> findByRole(Role role);

    /**
     * Find active users by role
     * Buscar usuarios activos por rol
     */
    List<User> findByRoleAndActiveTrue(Role role);

    /**
     * Find all active users
     * Buscar todos los usuarios activos
     */
    List<User> findByActiveTrue();

    /**
     * Find users by last name containing a search term (case-insensitive)
     * Buscar usuarios por apellido que contenga un termino de busqueda (sin distincion de mayusculas)
     */
    List<User> findByLastNameContainingIgnoreCase(String lastName);

    /**
     * Find users by first name or last name containing a search term (case-insensitive)
     * Buscar usuarios por nombre o apellido que contenga un termino de busqueda
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :term, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<User> searchByName(@Param("term") String term);

    /**
     * Find active user by email
     * Buscar usuario activo por email
     */
    Optional<User> findByEmailAndActiveTrue(String email);

    /**
     * Count users by role
     * Contar usuarios por rol
     */
    long countByRole(Role role);

    /**
     * Count active users
     * Contar usuarios activos
     */
    long countByActiveTrue();
}
