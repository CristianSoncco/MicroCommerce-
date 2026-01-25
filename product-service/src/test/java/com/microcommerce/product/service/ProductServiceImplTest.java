package com.microcommerce.product.service;

import com.microcommerce.product.dto.ProductDTO;
import com.microcommerce.product.entity.Product;
import com.microcommerce.product.exception.InsufficientStockException;
import com.microcommerce.product.exception.ProductAlreadyExistsException;
import com.microcommerce.product.exception.ProductNotFoundException;
import com.microcommerce.product.mapper.ProductMapper;
import com.microcommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests para ProductServiceImpl
 * Unit tests for ProductServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl Tests")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private RedisTemplate<String, Product> productRedisTemplate;

    @Mock
    private ValueOperations<String, Product> valueOperations;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        // Setup Product entity
        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .category("Electronics")
                .active(true)
                .build();

        // Setup ProductDTO
        productDTO = new ProductDTO();
        productDTO.setName("Test Product");
        productDTO.setDescription("Test Description");
        productDTO.setPrice(new BigDecimal("99.99"));
        productDTO.setStock(10);
        productDTO.setCategory("Electronics");
        productDTO.setActive(true);

        // Setup Redis mock
        when(productRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("createProduct - DTO válido - debe retornar producto creado")
    void createProduct_ValidDTO_ReturnsProduct() {
        // Given
        when(productRepository.existsByName(anyString())).thenReturn(false);
        when(productMapper.toEntity(any(ProductDTO.class))).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        Product result = productService.createProduct(productDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Product");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
        verify(productRepository).existsByName("Test Product");
        verify(productRepository).save(any(Product.class));
        verify(valueOperations).set(anyString(), any(Product.class), anyLong(), any());
    }

    @Test
    @DisplayName("createProduct - nombre duplicado - debe lanzar excepción")
    void createProduct_DuplicateName_ThrowsException() {
        // Given
        when(productRepository.existsByName(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> productService.createProduct(productDTO))
                .isInstanceOf(ProductAlreadyExistsException.class)
                .hasMessageContaining("Test Product");
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("getProductById - ID existente - debe retornar producto")
    void getProductById_ExistingId_ReturnsProduct() {
        // Given
        when(valueOperations.get(anyString())).thenReturn(null); // Cache miss
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // When
        Product result = productService.getProductById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Product");
        verify(productRepository).findById(1L);
        verify(valueOperations).set(anyString(), any(Product.class), anyLong(), any());
    }

    @Test
    @DisplayName("getProductById - producto en caché - debe retornar desde caché")
    void getProductById_CachedProduct_ReturnsFromCache() {
        // Given
        when(valueOperations.get(anyString())).thenReturn(product);

        // When
        Product result = productService.getProductById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Product");
        verify(productRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("getProductById - ID no existente - debe lanzar excepción")
    void getProductById_NonExistingId_ThrowsException() {
        // Given
        when(valueOperations.get(anyString())).thenReturn(null);
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("getAllProducts - debe retornar lista de productos")
    void getAllProducts_ReturnsProductList() {
        // Given
        List<Product> products = Arrays.asList(product, product);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<Product> result = productService.getAllProducts();

        // Then
        assertThat(result).hasSize(2);
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("updateProduct - datos válidos - debe actualizar producto")
    void updateProduct_ValidData_UpdatesProduct() {
        // Given
        when(valueOperations.get(anyString())).thenReturn(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        Product result = productService.updateProduct(1L, productDTO);

        // Then
        assertThat(result).isNotNull();
        verify(productMapper).updateEntityFromDto(productDTO, product);
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("deleteProduct - ID existente - debe eliminar producto")
    void deleteProduct_ExistingId_DeletesProduct() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(true);

        // When
        productService.deleteProduct(1L);

        // Then
        verify(productRepository).deleteById(1L);
        verify(productRedisTemplate).delete(anyString());
    }

    @Test
    @DisplayName("deleteProduct - ID no existente - debe lanzar excepción")
    void deleteProduct_NonExistingId_ThrowsException() {
        // Given
        when(productRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> productService.deleteProduct(999L))
                .isInstanceOf(ProductNotFoundException.class);
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("searchByName - nombre válido - debe retornar productos coincidentes")
    void searchByName_ValidName_ReturnsMatchingProducts() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByNameContainingIgnoreCase("Test")).thenReturn(products);

        // When
        List<Product> result = productService.searchByName("Test");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).contains("Test");
    }

    @Test
    @DisplayName("searchByCategory - categoría válida - debe retornar productos de la categoría")
    void searchByCategory_ValidCategory_ReturnsProductsByCategory() {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productRepository.findByCategoryAndActiveTrue("Electronics")).thenReturn(products);

        // When
        List<Product> result = productService.searchByCategory("Electronics");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("Electronics");
    }

    @Test
    @DisplayName("checkStock - stock suficiente - debe retornar true")
    void checkStock_SufficientStock_ReturnsTrue() {
        // Given
        when(valueOperations.get(anyString())).thenReturn(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // When
        boolean result = productService.checkStock(1L, 5);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("checkStock - stock insuficiente - debe retornar false")
    void checkStock_InsufficientStock_ReturnsFalse() {
        // Given
        when(valueOperations.get(anyString())).thenReturn(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // When
        boolean result = productService.checkStock(1L, 20);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("decreaseStock - stock suficiente - debe disminuir stock")
    void decreaseStock_SufficientStock_DecreasesStock() {
        // Given
        when(valueOperations.get(anyString())).thenReturn(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        productService.decreaseStock(1L, 5);

        // Then
        assertThat(product.getStock()).isEqualTo(5);
        verify(productRepository).save(product);
        verify(productRedisTemplate).delete(anyString());
    }

    @Test
    @DisplayName("decreaseStock - stock insuficiente - debe lanzar excepción")
    void decreaseStock_InsufficientStock_ThrowsException() {
        // Given
        when(valueOperations.get(anyString())).thenReturn(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // When & Then
        assertThatThrownBy(() -> productService.decreaseStock(1L, 20))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Stock insuficiente");
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("increaseStock - cantidad válida - debe aumentar stock")
    void increaseStock_ValidQuantity_IncreasesStock() {
        // Given
        when(valueOperations.get(anyString())).thenReturn(null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        productService.increaseStock(1L, 5);

        // Then
        assertThat(product.getStock()).isEqualTo(15);
        verify(productRepository).save(product);
        verify(productRedisTemplate).delete(anyString());
    }

    @Test
    @DisplayName("getLowStockProducts - debe retornar productos con stock bajo")
    void getLowStockProducts_ReturnsLowStockProducts() {
        // Given
        List<Product> lowStockProducts = Arrays.asList(product);
        when(productRepository.findLowStockProducts(10)).thenReturn(lowStockProducts);

        // When
        List<Product> result = productService.getLowStockProducts(10);

        // Then
        assertThat(result).hasSize(1);
        verify(productRepository).findLowStockProducts(10);
    }

    @Test
    @DisplayName("countByCategory - debe retornar cantidad de productos por categoría")
    void countByCategory_ReturnsProductCount() {
        // Given
        when(productRepository.countByCategory("Electronics")).thenReturn(5L);

        // When
        long result = productService.countByCategory("Electronics");

        // Then
        assertThat(result).isEqualTo(5L);
        verify(productRepository).countByCategory("Electronics");
    }
}
