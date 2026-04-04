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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of UserService with JWT authentication
 * Implementacion de UserService con autenticacion JWT
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // ==================== Authentication ====================

    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Registrando nuevo usuario: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );

        log.info("Usuario registrado satisfactoriamente: {}", savedUser.getEmail());
        return AuthResponse.of(token, savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Intento de inicio de sesion: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.getActive()) {
            throw new UserDisabledException(user.getEmail());
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        log.info("Inicio de sesion exitoso: {}", user.getEmail());
        return AuthResponse.of(token, user.getId(), user.getEmail(), user.getRole().name());
    }

    // ==================== CRUD Operations ====================

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        log.debug("Obteniendo usuario con ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        log.debug("Obteniendo usuario con email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        log.debug("Obteniendo todos los usuarios");
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getActiveUsers() {
        log.debug("Obteniendo todos los usuarios activos");
        return userRepository.findByActiveTrue();
    }

    @Override
    public User updateUser(Long id, UserDTO dto) {
        log.info("Actualizando usuario con ID: {}", id);

        User user = getUserById(id);

        // Check if email is being changed and is already taken
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException(dto.getEmail());
        }

        userMapper.updateEntityFromDto(dto, user);

        // Encode password if it was updated
        if (dto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        log.info("Usuario actualizado satisfactoriamente: {}", id);
        return updatedUser;
    }

    @Override
    public void deactivateUser(Long id) {
        log.info("Desactivando usuario con ID: {}", id);

        User user = getUserById(id);
        user.setActive(false);
        userRepository.save(user);

        log.info("Usuario desactivado satisfactoriamente: {}", id);
    }

    // ==================== Query Operations ====================

    @Override
    @Transactional(readOnly = true)
    public List<User> searchByName(String term) {
        log.debug("Buscando usuarios por nombre: {}", term);
        return userRepository.searchByName(term);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(Role role) {
        log.debug("Obteniendo usuarios por rol: {}", role);
        return userRepository.findByRole(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getActiveUsersByRole(Role role) {
        log.debug("Obteniendo usuarios activos por rol: {}", role);
        return userRepository.findByRoleAndActiveTrue(role);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRole(Role role) {
        log.debug("Contando usuarios por rol: {}", role);
        return userRepository.countByRole(role);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveUsers() {
        log.debug("Contando usuarios activos");
        return userRepository.countByActiveTrue();
    }
}
