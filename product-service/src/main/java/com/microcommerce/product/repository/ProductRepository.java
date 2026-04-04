package com.microcommerce.product.repository;

import com.microcommerce.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository interface for Product entity
 * Interfaz de repositorio para la entidad Product
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find products by category
     * Buscar productos por categoría
     */
    List<Product> findByCategory(String category);

    /**
     * Find products by category and active status
     * Buscar productos por categoría y estado activo
     */
    List<Product> findByCategoryAndActiveTrue(String category);

    /**
     * Find products by category and active status, ordered by creation date descending
     * Buscar productos por categoría y estado activo, ordenados por fecha de creación descendente
     */
    List<Product> findByCategoryAndActiveTrueOrderByCreatedAtDesc(String category);

    /**
     * Find a product by exact name
     * Buscar un producto por nombre exacto
     */
    java.util.Optional<Product> findByName(String name);

    /**
     * Find products within a price range
     * Buscar productos dentro de un rango de precio
     */
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Find products by name containing a search term (case-insensitive)
     * Buscar productos por nombre que contenga un término de búsqueda (sin distinción de mayúsculas)
     */
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Find all active products with stock available
     * Buscar todos los productos activos con stock disponible
     */
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stock > 0")
    List<Product> findAvailableProducts();

    /**
     * Find products by category with custom query
     * Buscar productos por categoría con query personalizado
     */
    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.active = true ORDER BY p.createdAt DESC")
    List<Product> findActiveByCategoryOrderByNewest(@Param("category") String category);

    /**
     * Check if product exists by name
     * Verificar si existe un producto por nombre
     */
    boolean existsByName(String name);

    /**
     * Count products by category
     * Contar productos por categoría
     */
    long countByCategory(String category);

    /**
     * Find products with low stock (less than specified quantity)
     * Buscar productos con stock bajo (menos de la cantidad especificada)
     */
    @Query("SELECT p FROM Product p WHERE p.stock < :threshold AND p.active = true")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);
}

