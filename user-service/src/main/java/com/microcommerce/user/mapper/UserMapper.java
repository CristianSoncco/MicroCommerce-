package com.microcommerce.user.mapper;

import com.microcommerce.user.dto.UserDTO;
import com.microcommerce.user.dto.response.UserResponse;
import com.microcommerce.user.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between User entity and DTOs
 * Mapper para convertir entre entidad User y DTOs
 */
@Component
public class UserMapper {

    /**
     * Convert UserDTO to User entity
     * Convertir UserDTO a entidad User
     */
    public User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());

        return user;
    }

    /**
     * Update User entity from UserDTO
     * Actualizar entidad User desde UserDTO
     */
    public void updateEntityFromDto(UserDTO dto, User user) {
        if (dto == null || user == null) {
            return;
        }

        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        // Password is handled by the service layer (encoded with BCrypt)
        // La contrasena se maneja en la capa de servicio (codificada con BCrypt)
        if (dto.getFirstName() != null) {
            user.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            user.setLastName(dto.getLastName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getAddress() != null) {
            user.setAddress(dto.getAddress());
        }
    }

    /**
     * Convert User entity to UserDTO
     * Convertir entidad User a UserDTO
     */
    public UserDTO toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        // Password is intentionally excluded from DTO conversion
        // La contrasena se excluye intencionalmente de la conversion a DTO

        return dto;
    }

    /**
     * Convert User entity to UserResponse
     * Convertir entidad User a UserResponse
     */
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
