package com.microcommerce.user.repository;

import com.microcommerce.user.entity.Role;
import com.microcommerce.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for UserRepository using TestContainers
 * Tests de integracion para UserRepository usando TestContainers
 */
@DataJpaTest
@Testcontainers
@ActiveProfiles("test-tc")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("UserRepository Integration Tests")
class UserRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Guardar usuario - debe persistir correctamente")
    void saveUser_ShouldPersistCorrectly() {
        // Given
        User user = createUser("john@example.com", "John", "Doe", Role.CUSTOMER, true);

        // When
        User saved = userRepository.save(user);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("john@example.com");
        assertThat(saved.getFirstName()).isEqualTo("John");
        assertThat(saved.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(saved.getActive()).isTrue();
    }

    @Test
    @DisplayName("findByEmail - debe encontrar usuario por email")
    void findByEmail_ShouldFindUserByEmail() {
        // Given
        createAndSaveUser("john@example.com", "John", "Doe", Role.CUSTOMER, true);

        // When
        Optional<User> found = userRepository.findByEmail("john@example.com");
        Optional<User> notFound = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
        assertThat(notFound).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail - debe retornar true si existe")
    void existsByEmail_ShouldReturnTrueIfExists() {
        // Given
        createAndSaveUser("john@example.com", "John", "Doe", Role.CUSTOMER, true);

        // When
        boolean exists = userRepository.existsByEmail("john@example.com");
        boolean notExists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("findByRole - debe retornar usuarios por rol")
    void findByRole_ShouldReturnUsersByRole() {
        // Given
        createAndSaveUser("customer1@example.com", "Customer", "One", Role.CUSTOMER, true);
        createAndSaveUser("customer2@example.com", "Customer", "Two", Role.CUSTOMER, true);
        createAndSaveUser("admin@example.com", "Admin", "User", Role.ADMIN, true);

        // When
        List<User> customers = userRepository.findByRole(Role.CUSTOMER);
        List<User> admins = userRepository.findByRole(Role.ADMIN);

        // Then
        assertThat(customers).hasSize(2);
        assertThat(admins).hasSize(1);
        assertThat(customers).allMatch(u -> u.getRole() == Role.CUSTOMER);
        assertThat(admins).allMatch(u -> u.getRole() == Role.ADMIN);
    }

    @Test
    @DisplayName("findByRoleAndActiveTrue - debe retornar solo usuarios activos del rol")
    void findByRoleAndActiveTrue_ShouldReturnOnlyActiveUsersByRole() {
        // Given
        createAndSaveUser("active@example.com", "Active", "Customer", Role.CUSTOMER, true);
        createAndSaveUser("inactive@example.com", "Inactive", "Customer", Role.CUSTOMER, false);
        createAndSaveUser("admin@example.com", "Admin", "User", Role.ADMIN, true);

        // When
        List<User> activeCustomers = userRepository.findByRoleAndActiveTrue(Role.CUSTOMER);

        // Then
        assertThat(activeCustomers).hasSize(1);
        assertThat(activeCustomers.get(0).getEmail()).isEqualTo("active@example.com");
        assertThat(activeCustomers.get(0).getActive()).isTrue();
    }

    @Test
    @DisplayName("findByActiveTrue - debe retornar todos los usuarios activos")
    void findByActiveTrue_ShouldReturnAllActiveUsers() {
        // Given
        createAndSaveUser("active1@example.com", "Active", "One", Role.CUSTOMER, true);
        createAndSaveUser("active2@example.com", "Active", "Two", Role.ADMIN, true);
        createAndSaveUser("inactive@example.com", "Inactive", "User", Role.CUSTOMER, false);

        // When
        List<User> activeUsers = userRepository.findByActiveTrue();

        // Then
        assertThat(activeUsers).hasSize(2);
        assertThat(activeUsers).allMatch(u -> u.getActive());
    }

    @Test
    @DisplayName("findByLastNameContainingIgnoreCase - debe buscar ignorando mayusculas")
    void findByLastNameContainingIgnoreCase_ShouldSearchCaseInsensitive() {
        // Given
        createAndSaveUser("john@example.com", "John", "Doe", Role.CUSTOMER, true);
        createAndSaveUser("jane@example.com", "Jane", "Doe", Role.CUSTOMER, true);
        createAndSaveUser("bob@example.com", "Bob", "Smith", Role.CUSTOMER, true);

        // When
        List<User> does = userRepository.findByLastNameContainingIgnoreCase("doe");

        // Then
        assertThat(does).hasSize(2);
        assertThat(does).allMatch(u -> u.getLastName().equalsIgnoreCase("Doe"));
    }

    @Test
    @DisplayName("searchByName - debe buscar por nombre o apellido")
    void searchByName_ShouldSearchByFirstOrLastName() {
        // Given
        createAndSaveUser("john@example.com", "John", "Doe", Role.CUSTOMER, true);
        createAndSaveUser("jane@example.com", "Jane", "Johnson", Role.CUSTOMER, true);
        createAndSaveUser("bob@example.com", "Bob", "Smith", Role.CUSTOMER, true);

        // When
        List<User> johResults = userRepository.searchByName("Joh");

        // Then
        assertThat(johResults).hasSize(2); // John (firstName) and Jane Johnson (lastName)
    }

    @Test
    @DisplayName("findByEmailAndActiveTrue - debe encontrar usuario activo por email")
    void findByEmailAndActiveTrue_ShouldFindActiveUserByEmail() {
        // Given
        createAndSaveUser("active@example.com", "Active", "User", Role.CUSTOMER, true);
        createAndSaveUser("inactive@example.com", "Inactive", "User", Role.CUSTOMER, false);

        // When
        Optional<User> activeFound = userRepository.findByEmailAndActiveTrue("active@example.com");
        Optional<User> inactiveNotFound = userRepository.findByEmailAndActiveTrue("inactive@example.com");

        // Then
        assertThat(activeFound).isPresent();
        assertThat(inactiveNotFound).isEmpty();
    }

    @Test
    @DisplayName("countByRole - debe contar usuarios por rol")
    void countByRole_ShouldCountUsersByRole() {
        // Given
        createAndSaveUser("c1@example.com", "Customer", "One", Role.CUSTOMER, true);
        createAndSaveUser("c2@example.com", "Customer", "Two", Role.CUSTOMER, true);
        createAndSaveUser("a1@example.com", "Admin", "One", Role.ADMIN, true);

        // When
        long customerCount = userRepository.countByRole(Role.CUSTOMER);
        long adminCount = userRepository.countByRole(Role.ADMIN);

        // Then
        assertThat(customerCount).isEqualTo(2);
        assertThat(adminCount).isEqualTo(1);
    }

    @Test
    @DisplayName("countByActiveTrue - debe contar usuarios activos")
    void countByActiveTrue_ShouldCountActiveUsers() {
        // Given
        createAndSaveUser("active1@example.com", "Active", "One", Role.CUSTOMER, true);
        createAndSaveUser("active2@example.com", "Active", "Two", Role.CUSTOMER, true);
        createAndSaveUser("inactive@example.com", "Inactive", "One", Role.CUSTOMER, false);

        // When
        long activeCount = userRepository.countByActiveTrue();

        // Then
        assertThat(activeCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Actualizar usuario - debe persistir cambios")
    void updateUser_ShouldPersistChanges() {
        // Given
        User user = createAndSaveUser("original@example.com", "Original", "Name", Role.CUSTOMER, true);
        Long userId = user.getId();

        // When
        user.setFirstName("Updated");
        user.setLastName("User");
        user.setPhone("+1234567890");
        userRepository.save(user);

        // Then
        User updated = userRepository.findById(userId).orElseThrow();
        assertThat(updated.getFirstName()).isEqualTo("Updated");
        assertThat(updated.getLastName()).isEqualTo("User");
        assertThat(updated.getPhone()).isEqualTo("+1234567890");
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Eliminar usuario - debe remover de base de datos")
    void deleteUser_ShouldRemoveFromDatabase() {
        // Given
        User user = createAndSaveUser("todelete@example.com", "To", "Delete", Role.CUSTOMER, true);
        Long userId = user.getId();

        // When
        userRepository.deleteById(userId);

        // Then
        Optional<User> deleted = userRepository.findById(userId);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("findByRole - rol sin usuarios - debe retornar lista vacia")
    void findByRole_EmptyRole_ShouldReturnEmptyList() {
        // When
        List<User> admins = userRepository.findByRole(Role.ADMIN);

        // Then
        assertThat(admins).isEmpty();
    }

    @Test
    @DisplayName("searchByName - sin resultados - debe retornar lista vacia")
    void searchByName_NoResults_ShouldReturnEmptyList() {
        // Given
        createAndSaveUser("john@example.com", "John", "Doe", Role.CUSTOMER, true);

        // When
        List<User> results = userRepository.searchByName("NonExistent");

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Desactivar usuario - soft delete - debe cambiar estado active")
    void deactivateUser_SoftDelete_ShouldChangeActiveStatus() {
        // Given
        User user = createAndSaveUser("todeactivate@example.com", "To", "Deactivate", Role.CUSTOMER, true);
        Long userId = user.getId();

        // When
        user.setActive(false);
        userRepository.save(user);

        // Then
        User deactivated = userRepository.findById(userId).orElseThrow();
        assertThat(deactivated.getActive()).isFalse();

        // Verify findByActiveTrue does not include this user
        List<User> activeUsers = userRepository.findByActiveTrue();
        assertThat(activeUsers).noneMatch(u -> u.getId().equals(userId));
    }

    // Helper methods
    private User createUser(String email, String firstName, String lastName, Role role, boolean active) {
        return User.builder()
                .email(email)
                .password("$2a$10$encodedPassword")
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .active(active)
                .build();
    }

    private User createAndSaveUser(String email, String firstName, String lastName, Role role, boolean active) {
        User user = createUser(email, firstName, lastName, role, active);
        return userRepository.save(user);
    }
}
