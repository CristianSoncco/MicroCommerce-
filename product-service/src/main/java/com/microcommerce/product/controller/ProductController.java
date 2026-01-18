package com.microcommerce.product.controller;

import com.microcommerce.product.dto.ProductDTO;
import com.microcommerce.product.dto.response.ApiResponse;
import com.microcommerce.product.dto.response.ProductResponse;
import com.microcommerce.product.entity.Product;
import com.microcommerce.product.mapper.ProductMapper;
import com.microcommerce.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Product operations
 * Controlador REST para operaciones de Producto
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "API de gestión de productos | Product management API")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    /**
     * Create a new product
     * Crear un nuevo producto
     */
    @PostMapping
    @Operation(
        summary = "Crear producto | Create product",
        description = "Crea un nuevo producto en el catálogo | Creates a new product in the catalog"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Producto creado exitosamente | Product created successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos | Invalid input data"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Producto ya existe | Product already exists"
        )
    })
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductDTO productDTO) {
        
        log.info("Solicitud para crear producto: {}", productDTO.getName());
        
        Product product = productService.createProduct(productDTO);
        ProductResponse response = productMapper.toResponse(product);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Producto creado exitosamente", response));
    }

    /**
     * Get product by ID
     * Obtener producto por ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener producto por ID | Get product by ID",
        description = "Obtiene un producto por su ID | Gets a product by its ID"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Producto encontrado | Product found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado | Product not found"
        )
    })
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @Parameter(description = "ID del producto | Product ID")
            @PathVariable Long id) {
        
        log.info("Solicitud para obtener producto con ID: {}", id);
        
        Product product = productService.getProductById(id);
        ProductResponse response = productMapper.toResponse(product);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all products
     * Obtener todos los productos
     */
    @GetMapping
    @Operation(
        summary = "Listar todos los productos | List all products",
        description = "Obtiene la lista de todos los productos | Gets the list of all products"
    )
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        
        log.info("Solicitud para obtener todos los productos");
        
        List<Product> products = productService.getAllProducts();
        List<ProductResponse> responses = products.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get active products
     * Obtener productos activos
     */
    @GetMapping("/active")
    @Operation(
        summary = "Listar productos activos | List active products",
        description = "Obtiene la lista de productos activos con stock | Gets the list of active products with stock"
    )
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getActiveProducts() {
        
        log.info("Solicitud para obtener productos activos");
        
        List<Product> products = productService.getActiveProducts();
        List<ProductResponse> responses = products.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Update product
     * Actualizar producto
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar producto | Update product",
        description = "Actualiza un producto existente | Updates an existing product"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Producto actualizado | Product updated"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado | Product not found"
        )
    })
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @Parameter(description = "ID del producto | Product ID")
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        
        log.info("Solicitud para actualizar producto con ID: {}", id);
        
        Product product = productService.updateProduct(id, productDTO);
        ProductResponse response = productMapper.toResponse(product);
        
        return ResponseEntity.ok(ApiResponse.success("Producto actualizado exitosamente", response));
    }

    /**
     * Delete product
     * Eliminar producto
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar producto | Delete product",
        description = "Elimina un producto del catálogo | Deletes a product from the catalog"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Producto eliminado | Product deleted"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado | Product not found"
        )
    })
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @Parameter(description = "ID del producto | Product ID")
            @PathVariable Long id) {
        
        log.info("Solicitud para eliminar producto con ID: {}", id);
        
        productService.deleteProduct(id);
        
        return ResponseEntity.ok(ApiResponse.success("Producto eliminado exitosamente", null));
    }

    /**
     * Search products by name
     * Buscar productos por nombre
     */
    @GetMapping("/search")
    @Operation(
        summary = "Buscar productos por nombre | Search products by name",
        description = "Busca productos que contengan el texto en su nombre | Searches for products containing the text in their name"
    )
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchByName(
            @Parameter(description = "Texto a buscar | Text to search")
            @RequestParam String name) {
        
        log.info("Solicitud para buscar productos por nombre: {}", name);
        
        List<Product> products = productService.searchByName(name);
        List<ProductResponse> responses = products.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Search products by category
     * Buscar productos por categoría
     */
    @GetMapping("/category/{category}")
    @Operation(
        summary = "Buscar productos por categoría | Search products by category",
        description = "Obtiene productos activos de una categoría específica | Gets active products from a specific category"
    )
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchByCategory(
            @Parameter(description = "Nombre de la categoría | Category name")
            @PathVariable String category) {
        
        log.info("Solicitud para buscar productos por categoría: {}", category);
        
        List<Product> products = productService.searchByCategory(category);
        List<ProductResponse> responses = products.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Search products by price range
     * Buscar productos por rango de precio
     */
    @GetMapping("/price-range")
    @Operation(
        summary = "Buscar productos por rango de precio | Search products by price range",
        description = "Obtiene productos dentro de un rango de precios | Gets products within a price range"
    )
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchByPriceRange(
            @Parameter(description = "Precio mínimo | Minimum price")
            @RequestParam BigDecimal minPrice,
            @Parameter(description = "Precio máximo | Maximum price")
            @RequestParam BigDecimal maxPrice) {
        
        log.info("Solicitud para buscar productos por rango de precio: {} - {}", minPrice, maxPrice);
        
        List<Product> products = productService.searchByPriceRange(minPrice, maxPrice);
        List<ProductResponse> responses = products.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get low stock products
     * Obtener productos con stock bajo
     */
    @GetMapping("/low-stock")
    @Operation(
        summary = "Obtener productos con stock bajo | Get low stock products",
        description = "Obtiene productos cuyo stock está por debajo del umbral | Gets products whose stock is below the threshold"
    )
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStockProducts(
            @Parameter(description = "Umbral de stock | Stock threshold")
            @RequestParam(defaultValue = "10") Integer threshold) {
        
        log.info("Solicitud para obtener productos con stock bajo: {}", threshold);
        
        List<Product> products = productService.getLowStockProducts(threshold);
        List<ProductResponse> responses = products.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Check stock availability
     * Verificar disponibilidad de stock
     */
    @GetMapping("/{id}/check-stock")
    @Operation(
        summary = "Verificar stock disponible | Check stock availability",
        description = "Verifica si hay suficiente stock del producto | Checks if there is enough stock of the product"
    )
    public ResponseEntity<ApiResponse<Boolean>> checkStock(
            @Parameter(description = "ID del producto | Product ID")
            @PathVariable Long id,
            @Parameter(description = "Cantidad requerida | Required quantity")
            @RequestParam Integer quantity) {
        
        log.info("Solicitud para verificar stock del producto {}: cantidad {}", id, quantity);
        
        boolean hasStock = productService.checkStock(id, quantity);
        String message = hasStock 
                ? "Stock disponible" 
                : "Stock insuficiente";
        
        return ResponseEntity.ok(ApiResponse.success(message, hasStock));
    }

    /**
     * Decrease product stock
     * Disminuir stock del producto
     */
    @PatchMapping("/{id}/decrease-stock")
    @Operation(
        summary = "Disminuir stock | Decrease stock",
        description = "Disminuye el stock de un producto | Decreases the stock of a product"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Stock disminuido | Stock decreased"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Stock insuficiente | Insufficient stock"
        )
    })
    public ResponseEntity<ApiResponse<Void>> decreaseStock(
            @Parameter(description = "ID del producto | Product ID")
            @PathVariable Long id,
            @Parameter(description = "Cantidad a disminuir | Quantity to decrease")
            @RequestParam Integer quantity) {
        
        log.info("Solicitud para disminuir stock del producto {}: cantidad {}", id, quantity);
        
        productService.decreaseStock(id, quantity);
        
        return ResponseEntity.ok(ApiResponse.success("Stock disminuido exitosamente", null));
    }

    /**
     * Increase product stock
     * Aumentar stock del producto
     */
    @PatchMapping("/{id}/increase-stock")
    @Operation(
        summary = "Aumentar stock | Increase stock",
        description = "Aumenta el stock de un producto | Increases the stock of a product"
    )
    public ResponseEntity<ApiResponse<Void>> increaseStock(
            @Parameter(description = "ID del producto | Product ID")
            @PathVariable Long id,
            @Parameter(description = "Cantidad a aumentar | Quantity to increase")
            @RequestParam Integer quantity) {
        
        log.info("Solicitud para aumentar stock del producto {}: cantidad {}", id, quantity);
        
        productService.increaseStock(id, quantity);
        
        return ResponseEntity.ok(ApiResponse.success("Stock aumentado exitosamente", null));
    }
}
