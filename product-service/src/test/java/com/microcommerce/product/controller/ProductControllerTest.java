package com.microcommerce.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microcommerce.product.dto.ProductDTO;
import com.microcommerce.product.dto.response.ProductResponse;
import com.microcommerce.product.entity.Product;
import com.microcommerce.product.exception.InsufficientStockException;
import com.microcommerce.product.exception.ProductAlreadyExistsException;
import com.microcommerce.product.exception.ProductNotFoundException;
import com.microcommerce.product.exception.handler.GlobalExceptionHandler;
import com.microcommerce.product.mapper.ProductMapper;
import com.microcommerce.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests para ProductController con MockMvc
 * Controller tests for ProductController with MockMvc
 */
@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
                "org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration"
})
@DisplayName("ProductController Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private ProductMapper productMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Product product;
    private ProductDTO productDTO;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .category("Electronics")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        productDTO = new ProductDTO();
        productDTO.setName("Test Product");
        productDTO.setDescription("Test Description");
        productDTO.setPrice(new BigDecimal("99.99"));
        productDTO.setStock(10);
        productDTO.setCategory("Electronics");
        productDTO.setActive(true);

        productResponse = ProductResponse.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stock(10)
                .category("Electronics")
                .active(true)
                .available(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/products - request válido - debe retornar 201")
    void createProduct_ValidRequest_Returns201() throws Exception {
        // Given
        when(productService.createProduct(any(ProductDTO.class))).thenReturn(product);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Producto creado exitosamente"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Product"))
                .andExpect(jsonPath("$.data.price").value(99.99));

        verify(productService).createProduct(any(ProductDTO.class));
    }

    @Test
    @DisplayName("POST /api/products - request inválido (sin nombre) - debe retornar 400")
    void createProduct_InvalidRequest_Returns400() throws Exception {
        // Given
        productDTO.setName(null); // Violación de validación

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"));

        verify(productService, never()).createProduct(any());
    }

    @Test
    @DisplayName("POST /api/products - producto duplicado - debe retornar 409")
    void createProduct_DuplicateProduct_Returns409() throws Exception {
        // Given
        when(productService.createProduct(any(ProductDTO.class)))
                .thenThrow(new ProductAlreadyExistsException("Test Product"));

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /api/products/{id} - ID existente - debe retornar 200")
    void getProduct_ExistingId_Returns200() throws Exception {
        // Given
        when(productService.getProductById(1L)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // When & Then
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Product"));

        verify(productService).getProductById(1L);
    }

    @Test
    @DisplayName("GET /api/products/{id} - ID no existente - debe retornar 404")
    void getProduct_NonExistingId_Returns404() throws Exception {
        // Given
        when(productService.getProductById(999L))
                .thenThrow(new ProductNotFoundException(999L));

        // When & Then
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /api/products - debe retornar lista de productos")
    void getAllProducts_ReturnsProductList() throws Exception {
        // Given
        List<Product> products = Arrays.asList(product, product);
        List<ProductResponse> responses = Arrays.asList(productResponse, productResponse);
        
        when(productService.getAllProducts()).thenReturn(products);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)));

        verify(productService).getAllProducts();
    }

    @Test
    @DisplayName("GET /api/products/active - debe retornar productos activos")
    void getActiveProducts_ReturnsActiveProducts() throws Exception {
        // Given
        List<Product> activeProducts = Arrays.asList(product);
        when(productService.getActiveProducts()).thenReturn(activeProducts);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        // When & Then
        mockMvc.perform(get("/api/products/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(productService).getActiveProducts();
    }

    @Test
    @DisplayName("PUT /api/products/{id} - request válido - debe retornar 200")
    void updateProduct_ValidRequest_Returns200() throws Exception {
        // Given
        when(productService.updateProduct(eq(1L), any(ProductDTO.class))).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // When & Then
        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Producto actualizado exitosamente"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(productService).updateProduct(eq(1L), any(ProductDTO.class));
    }

    @Test
    @DisplayName("PUT /api/products/{id} - ID no existente - debe retornar 404")
    void updateProduct_NonExistingId_Returns404() throws Exception {
        // Given
        when(productService.updateProduct(eq(999L), any(ProductDTO.class)))
                .thenThrow(new ProductNotFoundException(999L));

        // When & Then
        mockMvc.perform(put("/api/products/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - ID existente - debe retornar 200")
    void deleteProduct_ExistingId_Returns200() throws Exception {
        // Given
        doNothing().when(productService).deleteProduct(1L);

        // When & Then
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Producto eliminado exitosamente"));

        verify(productService).deleteProduct(1L);
    }

    @Test
    @DisplayName("GET /api/products/search - debe buscar por nombre")
    void searchByName_ReturnsMatchingProducts() throws Exception {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productService.searchByName("Test")).thenReturn(products);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        // When & Then
        mockMvc.perform(get("/api/products/search")
                        .param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(productService).searchByName("Test");
    }

    @Test
    @DisplayName("GET /api/products/category/{category} - debe buscar por categoría")
    void searchByCategory_ReturnsProductsByCategory() throws Exception {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productService.searchByCategory("Electronics")).thenReturn(products);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        // When & Then
        mockMvc.perform(get("/api/products/category/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(productService).searchByCategory("Electronics");
    }

    @Test
    @DisplayName("GET /api/products/price-range - debe buscar por rango de precio")
    void searchByPriceRange_ReturnsProductsInRange() throws Exception {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productService.searchByPriceRange(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(products);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        // When & Then
        mockMvc.perform(get("/api/products/price-range")
                        .param("minPrice", "50.00")
                        .param("maxPrice", "150.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(productService).searchByPriceRange(any(BigDecimal.class), any(BigDecimal.class));
    }

    @Test
    @DisplayName("GET /api/products/low-stock - debe retornar productos con stock bajo")
    void getLowStockProducts_ReturnsLowStockProducts() throws Exception {
        // Given
        List<Product> products = Arrays.asList(product);
        when(productService.getLowStockProducts(10)).thenReturn(products);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        // When & Then
        mockMvc.perform(get("/api/products/low-stock")
                        .param("threshold", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        verify(productService).getLowStockProducts(10);
    }

    @Test
    @DisplayName("GET /api/products/{id}/check-stock - stock disponible - debe retornar true")
    void checkStock_AvailableStock_ReturnsTrue() throws Exception {
        // Given
        when(productService.checkStock(1L, 5)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/products/1/check-stock")
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock disponible"))
                .andExpect(jsonPath("$.data").value(true));

        verify(productService).checkStock(1L, 5);
    }

    @Test
    @DisplayName("GET /api/products/{id}/check-stock - stock insuficiente - debe retornar false")
    void checkStock_InsufficientStock_ReturnsFalse() throws Exception {
        // Given
        when(productService.checkStock(1L, 20)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/products/1/check-stock")
                        .param("quantity", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock insuficiente"))
                .andExpect(jsonPath("$.data").value(false));

        verify(productService).checkStock(1L, 20);
    }

    @Test
    @DisplayName("PATCH /api/products/{id}/decrease-stock - stock suficiente - debe retornar 200")
    void decreaseStock_SufficientStock_Returns200() throws Exception {
        // Given
        doNothing().when(productService).decreaseStock(1L, 5);

        // When & Then
        mockMvc.perform(patch("/api/products/1/decrease-stock")
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock disminuido exitosamente"));

        verify(productService).decreaseStock(1L, 5);
    }

    @Test
    @DisplayName("PATCH /api/products/{id}/decrease-stock - stock insuficiente - debe retornar 400")
    void decreaseStock_InsufficientStock_Returns400() throws Exception {
        // Given
        doThrow(new InsufficientStockException(1L, 20, 10))
                .when(productService).decreaseStock(1L, 20);

        // When & Then
        mockMvc.perform(patch("/api/products/1/decrease-stock")
                        .param("quantity", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("PATCH /api/products/{id}/increase-stock - debe retornar 200")
    void increaseStock_ValidQuantity_Returns200() throws Exception {
        // Given
        doNothing().when(productService).increaseStock(1L, 10);

        // When & Then
        mockMvc.perform(patch("/api/products/1/increase-stock")
                        .param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock aumentado exitosamente"));

        verify(productService).increaseStock(1L, 10);
    }
}
