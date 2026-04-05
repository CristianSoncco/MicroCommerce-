package com.microcommerce.user.mapper;

import com.microcommerce.user.dto.UserDTO;
import com.microcommerce.user.dto.response.UserResponse;
import com.microcommerce.user.entity.Role;
import com.microcommerce.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests para UserMapper
 * Unit tests for UserMapper
 */
@DisplayName("UserMapper Tests")
class UserMapperTest {

    private UserMapper userMapper;
    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();

        // Setup User entity
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

        // Setup UserDTO
        userDTO = new UserDTO(
                "updated@example.com",
                "newpassword123",
                "Updated",
                "Name",
                "+5511888888888",
                "200 Updated Blvd"
        );
    }

    @Test
    @DisplayName("toEntity - DTO valido - debe convertir correctamente")
    void toEntity_ValidDTO_ConvertsCorrectly() {
        // When
        User result = userMapper.toEntity(userDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        assertThat(result.getPassword()).isEqualTo("newpassword123");
        assertThat(result.getFirstName()).isEqualTo("Updated");
        assertThat(result.getLastName()).isEqualTo("Name");
        assertThat(result.getPhone()).isEqualTo("+5511888888888");
        assertThat(result.getAddress()).isEqualTo("200 Updated Blvd");
        assertThat(result.getId()).isNull(); // ID no se mapea desde DTO
    }

    @Test
    @DisplayName("toEntity - DTO null - debe retornar null")
    void toEntity_NullDTO_ReturnsNull() {
        // When
        User result = userMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("toDto - entidad valida - debe convertir correctamente")
    void toDto_ValidEntity_ConvertsCorrectly() {
        // When
        UserDTO result = userMapper.toDto(user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getPhone()).isEqualTo("+1234567890");
        assertThat(result.getAddress()).isEqualTo("123 Main St");
        // Password should be excluded from DTO conversion
        assertThat(result.getPassword()).isNull();
    }

    @Test
    @DisplayName("toDto - entidad null - debe retornar null")
    void toDto_NullEntity_ReturnsNull() {
        // When
        UserDTO result = userMapper.toDto(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("toResponse - entidad valida - debe convertir correctamente")
    void toResponse_ValidEntity_ConvertsCorrectly() {
        // When
        UserResponse result = userMapper.toResponse(user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getPhone()).isEqualTo("+1234567890");
        assertThat(result.getAddress()).isEqualTo("123 Main St");
        assertThat(result.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(result.getActive()).isTrue();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("toResponse - entidad null - debe retornar null")
    void toResponse_NullEntity_ReturnsNull() {
        // When
        UserResponse result = userMapper.toResponse(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("updateEntityFromDto - debe actualizar solo campos no nulos")
    void updateEntityFromDto_UpdatesOnlyNonNullFields() {
        // Given
        UserDTO partialDTO = new UserDTO();
        partialDTO.setFirstName("UpdatedFirstName");
        partialDTO.setPhone("+9999999999");
        // Other fields are null

        // When
        userMapper.updateEntityFromDto(partialDTO, user);

        // Then
        assertThat(user.getFirstName()).isEqualTo("UpdatedFirstName");
        assertThat(user.getPhone()).isEqualTo("+9999999999");
        // Unchanged fields
        assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getAddress()).isEqualTo("123 Main St");
    }

    @Test
    @DisplayName("updateEntityFromDto - DTO null - no debe modificar entidad")
    void updateEntityFromDto_NullDTO_DoesNotModifyEntity() {
        // Given
        String originalName = user.getFirstName();

        // When
        userMapper.updateEntityFromDto(null, user);

        // Then
        assertThat(user.getFirstName()).isEqualTo(originalName);
    }

    @Test
    @DisplayName("updateEntityFromDto - entidad null - no debe lanzar excepcion")
    void updateEntityFromDto_NullEntity_DoesNotThrowException() {
        // When & Then
        assertThatCode(() -> userMapper.updateEntityFromDto(userDTO, null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("updateEntityFromDto - todos los campos - debe actualizar todos")
    void updateEntityFromDto_AllFields_UpdatesAll() {
        // When
        userMapper.updateEntityFromDto(userDTO, user);

        // Then
        assertThat(user.getEmail()).isEqualTo("updated@example.com");
        assertThat(user.getFirstName()).isEqualTo("Updated");
        assertThat(user.getLastName()).isEqualTo("Name");
        assertThat(user.getPhone()).isEqualTo("+5511888888888");
        assertThat(user.getAddress()).isEqualTo("200 Updated Blvd");
        // Password is NOT updated by mapper (handled by service layer)
        assertThat(user.getPassword()).isEqualTo("$2a$10$encodedPassword");
    }
}
