package com.microcommerce.user.service;

import com.microcommerce.user.dto.UserDTO;
import com.microcommerce.user.dto.request.LoginRequest;
import com.microcommerce.user.dto.request.RegisterRequest;
import com.microcommerce.user.dto.response.AuthResponse;
import com.microcommerce.user.entity.Role;
import com.microcommerce.user.entity.User;
import com.microcommerce.user.exception.InvalidCredentialsException;
import com.microcommerce.user.exception.UserAlreadyExistsException;
import com.microcommerce.user.exception.UserDisabledException;
import com.microcommerce.user.exception.UserNotFoundException;
import com.microcommerce.user.mapper.UserMapper;
import com.microcommerce.user.repository.UserRepository;
import com.microcommerce.user.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests para UserServiceImpl
 * Unit tests for UserServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private User adminUser;
    private User disabledUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        // Setup active customer user
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

        // Setup admin user
        adminUser = User.builder()
                .id(2L)
                .email("admin@microcommerce.com")
                .password("$2a$10$encodedAdminPassword")
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .active(true)
                .build();

        // Setup disabled user
        disabledUser = User.builder()
                .id(3L)
                .email("disabled@example.com")
                .password("$2a$10$encodedPassword")
                .firstName("Disabled")
                .lastName("Account")
                .role(Role.CUSTOMER)
                .active(false)
                .build();

        // Setup RegisterRequest
        registerRequest = new RegisterRequest(
                "newuser@example.com",
                "password123",
                "New",
                "User",
                "+5511999999999",
                "100 New Street"
        );

        // Setup LoginRequest
        loginRequest = new LoginRequest("john.doe@example.com", "password123");

        // Setup UserDTO
        userDTO = new UserDTO(
                "updated@example.com",
                "newpassword123",
                "Updated",
                "Name",
                "+5511888888888",
                "200 Updated Blvd"
        );

        // Common mock setups
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(jwtTokenProvider.generateToken(anyLong(), anyString(), anyString())).thenReturn("mock-jwt-token");
    }

    // ==================== Registration Tests ====================

    @Test
    @DisplayName("register - request valido - debe retornar AuthResponse con token")
    void register_ValidRequest_ReturnsAuthResponse() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // When
        AuthResponse result = userService.register(registerRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("mock-jwt-token");
        assertThat(result.getType()).isEqualTo("Bearer");
        assertThat(result.getEmail()).isEqualTo("newuser@example.com");
        assertThat(result.getRole()).isEqualTo("CUSTOMER");
        verify(userRepository).existsByEmail("newuser@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register - email duplicado - debe lanzar UserAlreadyExistsException")
    void register_DuplicateEmail_ThrowsException() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("newuser@example.com");
        verify(userRepository, never()).save(any());
    }

    // ==================== Login Tests ====================

    @Test
    @DisplayName("login - credenciales validas - debe retornar AuthResponse con token")
    void login_ValidCredentials_ReturnsAuthResponse() {
        // Given
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(true);

        // When
        AuthResponse result = userService.login(loginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("mock-jwt-token");
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.getRole()).isEqualTo("CUSTOMER");
    }

    @Test
    @DisplayName("login - email no existente - debe lanzar InvalidCredentialsException")
    void login_NonExistingEmail_ThrowsException() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("login - contrasena incorrecta - debe lanzar InvalidCredentialsException")
    void login_WrongPassword_ThrowsException() {
        // Given
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("login - usuario deshabilitado - debe lanzar UserDisabledException")
    void login_DisabledUser_ThrowsException() {
        // Given
        LoginRequest disabledLogin = new LoginRequest("disabled@example.com", "disabled123");
        when(userRepository.findByEmail("disabled@example.com")).thenReturn(Optional.of(disabledUser));

        // When & Then
        assertThatThrownBy(() -> userService.login(disabledLogin))
                .isInstanceOf(UserDisabledException.class)
                .hasMessageContaining("disabled@example.com");
    }

    // ==================== CRUD Operation Tests ====================

    @Test
    @DisplayName("getUserById - ID existente - debe retornar usuario")
    void getUserById_ExistingId_ReturnsUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        User result = userService.getUserById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("getUserById - ID no existente - debe lanzar UserNotFoundException")
    void getUserById_NonExistingId_ThrowsException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("getUserByEmail - email existente - debe retornar usuario")
    void getUserByEmail_ExistingEmail_ReturnsUser() {
        // Given
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));

        // When
        User result = userService.getUserByEmail("john.doe@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("getUserByEmail - email no existente - debe lanzar UserNotFoundException")
    void getUserByEmail_NonExistingEmail_ThrowsException() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByEmail("nonexistent@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("getAllUsers - debe retornar lista de usuarios")
    void getAllUsers_ReturnsUserList() {
        // Given
        List<User> users = Arrays.asList(user, adminUser);
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(2);
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("getAllUsers - lista vacia - debe retornar lista vacia")
    void getAllUsers_EmptyList_ReturnsEmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(List.of());

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getActiveUsers - debe retornar solo usuarios activos")
    void getActiveUsers_ReturnsActiveUsers() {
        // Given
        List<User> activeUsers = Arrays.asList(user, adminUser);
        when(userRepository.findByActiveTrue()).thenReturn(activeUsers);

        // When
        List<User> result = userService.getActiveUsers();

        // Then
        assertThat(result).hasSize(2);
        verify(userRepository).findByActiveTrue();
    }

    @Test
    @DisplayName("updateUser - datos validos - debe actualizar usuario")
    void updateUser_ValidData_UpdatesUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        User result = userService.updateUser(1L, userDTO);

        // Then
        assertThat(result).isNotNull();
        verify(userMapper).updateEntityFromDto(userDTO, user);
        verify(passwordEncoder).encode("newpassword123");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("updateUser - email duplicado - debe lanzar UserAlreadyExistsException")
    void updateUser_DuplicateEmail_ThrowsException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(1L, userDTO))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("updated@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUser - mismo email - no debe verificar duplicado")
    void updateUser_SameEmail_ShouldNotCheckDuplicate() {
        // Given
        UserDTO sameEmailDto = new UserDTO(
                "john.doe@example.com", null, "Updated", "Name", null, null
        );
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        User result = userService.updateUser(1L, sameEmailDto);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("updateUser - sin contrasena - no debe codificar contrasena")
    void updateUser_NullPassword_ShouldNotEncodePassword() {
        // Given
        UserDTO noPasswordDto = new UserDTO(
                "john.doe@example.com", null, "Updated", "Name", null, null
        );
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        userService.updateUser(1L, noPasswordDto);

        // Then
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("updateUser - ID no existente - debe lanzar UserNotFoundException")
    void updateUser_NonExistingId_ThrowsException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(999L, userDTO))
                .isInstanceOf(UserNotFoundException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("deactivateUser - ID existente - debe desactivar usuario")
    void deactivateUser_ExistingId_DeactivatesUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        userService.deactivateUser(1L);

        // Then
        assertThat(user.getActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("deactivateUser - ID no existente - debe lanzar UserNotFoundException")
    void deactivateUser_NonExistingId_ThrowsException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.deactivateUser(999L))
                .isInstanceOf(UserNotFoundException.class);
        verify(userRepository, never()).save(any());
    }

    // ==================== Query Operation Tests ====================

    @Test
    @DisplayName("searchByName - termino valido - debe retornar usuarios coincidentes")
    void searchByName_ValidTerm_ReturnsMatchingUsers() {
        // Given
        List<User> users = Arrays.asList(user);
        when(userRepository.searchByName("Doe")).thenReturn(users);

        // When
        List<User> result = userService.searchByName("Doe");

        // Then
        assertThat(result).hasSize(1);
        verify(userRepository).searchByName("Doe");
    }

    @Test
    @DisplayName("searchByName - sin resultados - debe retornar lista vacia")
    void searchByName_NoResults_ReturnsEmptyList() {
        // Given
        when(userRepository.searchByName("NonExistent")).thenReturn(List.of());

        // When
        List<User> result = userService.searchByName("NonExistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getUsersByRole - debe retornar usuarios del rol especificado")
    void getUsersByRole_ReturnsUsersByRole() {
        // Given
        List<User> customers = Arrays.asList(user);
        when(userRepository.findByRole(Role.CUSTOMER)).thenReturn(customers);

        // When
        List<User> result = userService.getUsersByRole(Role.CUSTOMER);

        // Then
        assertThat(result).hasSize(1);
        verify(userRepository).findByRole(Role.CUSTOMER);
    }

    @Test
    @DisplayName("getActiveUsersByRole - debe retornar usuarios activos del rol")
    void getActiveUsersByRole_ReturnsActiveUsersByRole() {
        // Given
        List<User> activeCustomers = Arrays.asList(user);
        when(userRepository.findByRoleAndActiveTrue(Role.CUSTOMER)).thenReturn(activeCustomers);

        // When
        List<User> result = userService.getActiveUsersByRole(Role.CUSTOMER);

        // Then
        assertThat(result).hasSize(1);
        verify(userRepository).findByRoleAndActiveTrue(Role.CUSTOMER);
    }

    @Test
    @DisplayName("countByRole - debe retornar cantidad de usuarios por rol")
    void countByRole_ReturnsUserCount() {
        // Given
        when(userRepository.countByRole(Role.CUSTOMER)).thenReturn(5L);

        // When
        long result = userService.countByRole(Role.CUSTOMER);

        // Then
        assertThat(result).isEqualTo(5L);
        verify(userRepository).countByRole(Role.CUSTOMER);
    }

    @Test
    @DisplayName("countActiveUsers - debe retornar cantidad de usuarios activos")
    void countActiveUsers_ReturnsActiveUserCount() {
        // Given
        when(userRepository.countByActiveTrue()).thenReturn(10L);

        // When
        long result = userService.countActiveUsers();

        // Then
        assertThat(result).isEqualTo(10L);
        verify(userRepository).countByActiveTrue();
    }
}
