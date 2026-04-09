package com.microcommerce.product.mapper;

import com.microcommerce.product.dto.ProductDTO;
import com.microcommerce.product.dto.response.ProductResponse;
import com.microcommerce.product.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests para ProductMapper
 * Unit tests for ProductMapper
 */
@DisplayName("ProductMapper Tests")
class ProductMapperTest {

    private ProductMapper productMapper;
    private Product product;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        productMapper = new ProductMapper();

        // Setup Product entity
        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .category("Electronics")
                .imageUrl("http://example.com/image.jpg")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Setup ProductDTO
        productDTO = new ProductDTO();
        productDTO.setName("DTO Product");
        productDTO.setDescription("DTO Description");
        productDTO.setPrice(new BigDecimal("199.99"));
        productDTO.setStock(20);
        productDTO.setCategory("Books");
        productDTO.setImageUrl("http://example.com/dto.jpg");
        productDTO.setActive(false);
    }

    @Test
    @DisplayName("toEntity - DTO válido - debe convertir correctamente")
    void toEntity_ValidDTO_ConvertsCorrectly() {
        // When
        Product result = productMapper.toEntity(productDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("DTO Product");
        assertThat(result.getDescription()).isEqualTo("DTO Description");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("199.99"));
        assertThat(result.getStock()).isEqualTo(20);
        assertThat(result.getCategory()).isEqualTo("Books");
        assertThat(result.getImageUrl()).isEqualTo("http://example.com/dto.jpg");
        assertThat(result.getActive()).isFalse();
        assertThat(result.getId()).isNull(); // ID no se mapea desde DTO
    }

    @Test
    @DisplayName("toEntity - DTO con active null - debe usar true por defecto")
    void toEntity_DTOWithNullActive_UsesDefaultTrue() {
        // Given
        productDTO.setActive(null);

        // When
        Product result = productMapper.toEntity(productDTO);

        // Then
        assertThat(result.getActive()).isTrue();
    }

    @Test
    @DisplayName("toEntity - DTO null - debe retornar null")
    void toEntity_NullDTO_ReturnsNull() {
        // When
        Product result = productMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("toDto - entidad válida - debe convertir correctamente")
    void toDto_ValidEntity_ConvertsCorrectly() {
        // When
        ProductDTO result = productMapper.toDto(product);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Product");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(result.getStock()).isEqualTo(10);
        assertThat(result.getCategory()).isEqualTo("Electronics");
        assertThat(result.getImageUrl()).isEqualTo("http://example.com/image.jpg");
        assertThat(result.getActive()).isTrue();
    }

    @Test
    @DisplayName("toDto - entidad null - debe retornar null")
    void toDto_NullEntity_ReturnsNull() {
        // When
        ProductDTO result = productMapper.toDto(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("toResponse - entidad válida - debe convertir correctamente")
    void toResponse_ValidEntity_ConvertsCorrectly() {
        // When
        ProductResponse result = productMapper.toResponse(product);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Product");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(result.getStock()).isEqualTo(10);
        assertThat(result.getCategory()).isEqualTo("Electronics");
        assertThat(result.getImageUrl()).isEqualTo("http://example.com/image.jpg");
        assertThat(result.getActive()).isTrue();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        assertThat(result.getAvailable()).isTrue(); // active=true && stock>0
    }

    @Test
    @DisplayName("toResponse - producto con stock 0 - available debe ser false")
    void toResponse_ProductWithZeroStock_AvailableIsFalse() {
        // Given
        product.setStock(0);

        // When
        ProductResponse result = productMapper.toResponse(product);

        // Then
        assertThat(result.getAvailable()).isFalse();
    }

    @Test
    @DisplayName("toResponse - producto inactivo - available debe ser false")
    void toResponse_InactiveProduct_AvailableIsFalse() {
        // Given
        product.setActive(false);

        // When
        ProductResponse result = productMapper.toResponse(product);

        // Then
        assertThat(result.getAvailable()).isFalse();
    }

    @Test
    @DisplayName("toResponse - entidad null - debe retornar null")
    void toResponse_NullEntity_ReturnsNull() {
        // When
        ProductResponse result = productMapper.toResponse(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("updateEntityFromDto - debe actualizar solo campos no nulos")
    void updateEntityFromDto_UpdatesOnlyNonNullFields() {
        // Given
        ProductDTO partialDTO = new ProductDTO();
        partialDTO.setName("Updated Name");
        partialDTO.setPrice(new BigDecimal("299.99"));
        // Otros campos null

        // When
        productMapper.updateEntityFromDto(partialDTO, product);

        // Then
        assertThat(product.getName()).isEqualTo("Updated Name");
        assertThat(product.getPrice()).isEqualByComparingTo(new BigDecimal("299.99"));
        assertThat(product.getDescription()).isEqualTo("Test Description"); // No cambió
        assertThat(product.getStock()).isEqualTo(10); // No cambió
        assertThat(product.getCategory()).isEqualTo("Electronics"); // No cambió
    }

    @Test
    @DisplayName("updateEntityFromDto - DTO null - no debe modificar entidad")
    void updateEntityFromDto_NullDTO_DoesNotModifyEntity() {
        // Given
        String originalName = product.getName();

        // When
        productMapper.updateEntityFromDto(null, product);

        // Then
        assertThat(product.getName()).isEqualTo(originalName);
    }

    @Test
    @DisplayName("updateEntityFromDto - entidad null - no debe lanzar excepción")
    void updateEntityFromDto_NullEntity_DoesNotThrowException() {
        // When & Then
        assertThatCode(() -> productMapper.updateEntityFromDto(productDTO, null))
                .doesNotThrowAnyException();
    }
}
