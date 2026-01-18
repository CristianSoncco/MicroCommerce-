package com.microcommerce.product.service;

import com.microcommerce.product.dto.ProductDTO;
import com.microcommerce.product.entity.Product;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for Product operations
 * Interfaz de servicio para operaciones de Product
 */
public interface ProductService {

    /**
     * Create a new product
     * Crear un nuevo producto
     */
    Product createProduct(ProductDTO dto);

    /**
     * Get product by ID (with caching)
     * Obtener producto por ID (con caché)
     */
    Product getProductById(Long id);

    /**
     * Get all products
     * Obtener todos los productos
     */
    List<Product> getAllProducts();

    /**
     * Get all active products
     * Obtener todos los productos activos
     */
    List<Product> getActiveProducts();

    /**
     * Update product
     * Actualizar producto
     */
    Product updateProduct(Long id, ProductDTO dto);

    /**
     * Delete product
     * Eliminar producto
     */
    void deleteProduct(Long id);

    /**
     * Search products by name
     * Buscar productos por nombre
     */
    List<Product> searchByName(String name);

    /**
     * Search products by category
     * Buscar productos por categoría
     */
    List<Product> searchByCategory(String category);

    /**
     * Search products by price range
     * Buscar productos por rango de precio
     */
    List<Product> searchByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Get available products (active with stock)
     * Obtener productos disponibles (activos con stock)
     */
    List<Product> getAvailableProducts();

    /**
     * Check if product has sufficient stock
     * Verificar si el producto tiene stock suficiente
     */
    boolean checkStock(Long productId, Integer quantity);

    /**
     * Decrease product stock
     * Disminuir stock del producto
     */
    void decreaseStock(Long productId, Integer quantity);

    /**
     * Increase product stock
     * Aumentar stock del producto
     */
    void increaseStock(Long productId, Integer quantity);

    /**
     * Get products with low stock
     * Obtener productos con stock bajo
     */
    List<Product> getLowStockProducts(Integer threshold);

    /**
     * Count products by category
     * Contar productos por categoría
     */
    long countByCategory(String category);
}
