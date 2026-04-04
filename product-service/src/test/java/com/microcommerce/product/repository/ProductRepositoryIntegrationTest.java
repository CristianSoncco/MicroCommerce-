package com.microcommerce.product.repository;

import com.microcommerce.product.entity.Product;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for ProductRepository using TestContainers
 * Tests de integracion para ProductRepository usando TestContainers
 */
@DataJpaTest
@Testcontainers
@ActiveProfiles("test-tc")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("ProductRepository Integration Tests")
class ProductRepositoryIntegrationTest {

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
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("Guardar producto - debe persistir correctamente")
    void saveProduct_ShouldPersistCorrectly() {
        // Given
        Product product = Product.builder()
                .name("Integration Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .category("Electronics")
                .active(true)
                .build();

        // When
        Product saved = productRepository.save(product);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Integration Test Product");
    }

    @Test
    @DisplayName("findByCategory - debe retornar productos de la categoria")
    void findByCategory_ShouldReturnProductsInCategory() {
        // Given
        createProduct("Product 1", "Electronics", true, 10);
        createProduct("Product 2", "Electronics", true, 5);
        createProduct("Product 3", "Books", true, 20);

        // When
        List<Product> electronics = productRepository.findByCategory("Electronics");

        // Then
        assertThat(electronics).hasSize(2);
        assertThat(electronics).allMatch(p -> p.getCategory().equals("Electronics"));
    }

    @Test
    @DisplayName("findByCategoryAndActiveTrue - debe retornar solo productos activos")
    void findByCategoryAndActiveTrue_ShouldReturnOnlyActiveProducts() {
        // Given
        createProduct("Active Product", "Electronics", true, 10);
        createProduct("Inactive Product", "Electronics", false, 5);

        // When
        List<Product> activeProducts = productRepository.findByCategoryAndActiveTrue("Electronics");

        // Then
        assertThat(activeProducts).hasSize(1);
        assertThat(activeProducts.get(0).getActive()).isTrue();
        assertThat(activeProducts.get(0).getName()).isEqualTo("Active Product");
    }

    @Test
    @DisplayName("findByPriceBetween - debe retornar productos en rango de precio")
    void findByPriceBetween_ShouldReturnProductsInPriceRange() {
        // Given
        createProduct("Cheap Product", "Electronics", true, 10, new BigDecimal("50.00"));
        createProduct("Mid Product", "Electronics", true, 10, new BigDecimal("100.00"));
        createProduct("Expensive Product", "Electronics", true, 10, new BigDecimal("200.00"));

        // When
        List<Product> midRange = productRepository.findByPriceBetween(
                new BigDecimal("80.00"),
                new BigDecimal("150.00")
        );

        // Then
        assertThat(midRange).hasSize(1);
        assertThat(midRange.get(0).getName()).isEqualTo("Mid Product");
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase - debe buscar ignorando mayusculas")
    void findByNameContainingIgnoreCase_ShouldSearchCaseInsensitive() {
        // Given
        createProduct("Laptop HP", "Electronics", true, 10);
        createProduct("Laptop Dell", "Electronics", true, 5);
        createProduct("Mouse Logitech", "Electronics", true, 20);

        // When
        List<Product> laptops = productRepository.findByNameContainingIgnoreCase("laptop");

        // Then
        assertThat(laptops).hasSize(2);
        assertThat(laptops).allMatch(p -> p.getName().toLowerCase().contains("laptop"));
    }

    @Test
    @DisplayName("findAvailableProducts - debe retornar productos activos con stock")
    void findAvailableProducts_ShouldReturnActiveProductsWithStock() {
        // Given
        createProduct("Available Product", "Electronics", true, 10);
        createProduct("No Stock Product", "Electronics", true, 0);
        createProduct("Inactive Product", "Electronics", false, 10);

        // When
        List<Product> available = productRepository.findAvailableProducts();

        // Then
        assertThat(available).hasSize(1);
        assertThat(available.get(0).getName()).isEqualTo("Available Product");
        assertThat(available.get(0).getStock()).isGreaterThan(0);
        assertThat(available.get(0).getActive()).isTrue();
    }

    @Test
    @DisplayName("findByCategoryAndActiveTrueOrderByCreatedAtDesc - debe ordenar por fecha")
    void findByCategoryAndActiveTrueOrderByCreatedAtDesc_ShouldOrderByDate() throws InterruptedException {
        // Given
        createProduct("Oldest Product", "Electronics", true, 10);
        Thread.sleep(10);
        createProduct("Newest Product", "Electronics", true, 5);

        // When
        List<Product> products = productRepository.findByCategoryAndActiveTrueOrderByCreatedAtDesc("Electronics");

        // Then
        assertThat(products).hasSize(2);
        assertThat(products.get(0).getName()).isEqualTo("Newest Product");
    }

    @Test
    @DisplayName("existsByName - debe retornar true si existe")
    void existsByName_ShouldReturnTrueIfExists() {
        // Given
        createProduct("Unique Product", "Electronics", true, 10);

        // When
        boolean exists = productRepository.existsByName("Unique Product");
        boolean notExists = productRepository.existsByName("Non Existing");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("countByCategory - debe contar productos por categoria")
    void countByCategory_ShouldCountProductsByCategory() {
        // Given
        createProduct("Product 1", "Electronics", true, 10);
        createProduct("Product 2", "Electronics", true, 5);
        createProduct("Product 3", "Books", true, 20);

        // When
        long electronicsCount = productRepository.countByCategory("Electronics");
        long booksCount = productRepository.countByCategory("Books");

        // Then
        assertThat(electronicsCount).isEqualTo(2);
        assertThat(booksCount).isEqualTo(1);
    }

    @Test
    @DisplayName("findLowStockProducts - debe retornar productos con stock bajo")
    void findLowStockProducts_ShouldReturnLowStockProducts() {
        // Given
        createProduct("Low Stock Product", "Electronics", true, 5);
        createProduct("High Stock Product", "Electronics", true, 50);
        createProduct("Zero Stock Product", "Electronics", true, 0);

        // When
        List<Product> lowStock = productRepository.findLowStockProducts(10);

        // Then
        assertThat(lowStock).hasSize(2);
        assertThat(lowStock).allMatch(p -> p.getStock() < 10);
        assertThat(lowStock).allMatch(Product::getActive);
    }

    @Test
    @DisplayName("findByName - debe encontrar producto por nombre exacto")
    void findByName_ShouldFindProductByExactName() {
        // Given
        createProduct("Exact Name Product", "Electronics", true, 10);

        // When
        Optional<Product> found = productRepository.findByName("Exact Name Product");
        Optional<Product> notFound = productRepository.findByName("Wrong Name");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Exact Name Product");
        assertThat(notFound).isEmpty();
    }

    @Test
    @DisplayName("Actualizar producto - debe persistir cambios")
    void updateProduct_ShouldPersistChanges() {
        // Given
        Product product = createProduct("Original Name", "Electronics", true, 10);
        Long productId = product.getId();

        // When
        product.setName("Updated Name");
        product.setStock(20);
        productRepository.save(product);

        // Then
        Product updated = productRepository.findById(productId).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getStock()).isEqualTo(20);
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Eliminar producto - debe remover de base de datos")
    void deleteProduct_ShouldRemoveFromDatabase() {
        // Given
        Product product = createProduct("To Delete", "Electronics", true, 10);
        Long productId = product.getId();

        // When
        productRepository.deleteById(productId);

        // Then
        Optional<Product> deleted = productRepository.findById(productId);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("findActiveByCategoryOrderByNewest - debe retornar productos activos ordenados")
    void findActiveByCategoryOrderByNewest_ShouldReturnActiveProductsOrdered() throws InterruptedException {
        // Given
        createProduct("First Product", "Electronics", true, 10);
        Thread.sleep(10);
        createProduct("Second Product", "Electronics", true, 5);
        createProduct("Inactive Product", "Electronics", false, 15);

        // When
        List<Product> products = productRepository.findActiveByCategoryOrderByNewest("Electronics");

        // Then
        assertThat(products).hasSize(2);
        assertThat(products.get(0).getName()).isEqualTo("Second Product");
        assertThat(products).allMatch(Product::getActive);
    }

    @Test
    @DisplayName("findByCategory - categoria sin productos - debe retornar lista vacia")
    void findByCategory_EmptyCategory_ShouldReturnEmptyList() {
        // When
        List<Product> products = productRepository.findByCategory("NonExistent");

        // Then
        assertThat(products).isEmpty();
    }

    @Test
    @DisplayName("findLowStockProducts - sin productos con stock bajo - debe retornar lista vacia")
    void findLowStockProducts_NoLowStock_ShouldReturnEmptyList() {
        // Given
        createProduct("High Stock", "Electronics", true, 100);

        // When
        List<Product> lowStock = productRepository.findLowStockProducts(5);

        // Then
        assertThat(lowStock).isEmpty();
    }

    // Helper methods
    private Product createProduct(String name, String category, boolean active, int stock) {
        return createProduct(name, category, active, stock, new BigDecimal("99.99"));
    }

    private Product createProduct(String name, String category, boolean active, int stock, BigDecimal price) {
        Product product = Product.builder()
                .name(name)
                .description("Test Description")
                .price(price)
                .stock(stock)
                .category(category)
                .active(active)
                .build();
        return productRepository.save(product);
    }
}
