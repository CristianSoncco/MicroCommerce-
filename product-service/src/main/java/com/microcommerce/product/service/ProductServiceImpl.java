package com.microcommerce.product.service;

import com.microcommerce.product.dto.ProductDTO;
import com.microcommerce.product.entity.Product;
import com.microcommerce.product.exception.InsufficientStockException;
import com.microcommerce.product.exception.ProductAlreadyExistsException;
import com.microcommerce.product.exception.ProductNotFoundException;
import com.microcommerce.product.mapper.ProductMapper;
import com.microcommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of ProductService with Redis caching
 * Implementación de ProductService con caché Redis
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final RedisTemplate<String, Product> productRedisTemplate;

    private static final String CACHE_KEY_PREFIX = "product:";
    private static final long CACHE_TTL_HOURS = 1;

    @Override
    public Product createProduct(ProductDTO dto) {
        log.info("Creando producto: {}", dto.getName());

        // Check if product already exists
        if (productRepository.existsByName(dto.getName())) {
            throw new ProductAlreadyExistsException(dto.getName());
        }

        Product product = productMapper.toEntity(dto);
        Product savedProduct = productRepository.save(product);

        // Cache the new product
        cacheProduct(savedProduct);

        log.info("Producto creado satisfactoriamente con el ID: {}", savedProduct.getId());
        return savedProduct;
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        log.debug("Obtener producto con ID: {}", id);

        // Try to get from cache
        String cacheKey = CACHE_KEY_PREFIX + id;
        Product cachedProduct = productRedisTemplate.opsForValue().get(cacheKey);

        if (cachedProduct != null) {
            log.debug("Producto encontrado en caché: {}", id);
            return cachedProduct;
        }

        // Cache miss - get from database
        log.debug("Producto no en caché, obteniendo de la base de datos: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        // Save to cache
        cacheProduct(product);

        return product;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        log.debug("Obteniendo todos los productos");
        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getActiveProducts() {
        log.debug("Obteniendo todos los productos activos");
        return productRepository.findAvailableProducts();
    }

    @Override
    public Product updateProduct(Long id, ProductDTO dto) {
        log.info("Actualizando producto con ID: {}", id);

        Product product = getProductById(id);
        productMapper.updateEntityFromDto(dto, product);
        Product updatedProduct = productRepository.save(product);

        // Update cache
        cacheProduct(updatedProduct);

        log.info("Producto actualizado satisfactoriamente: {}", id);
        return updatedProduct;
    }

    @Override
    public void deleteProduct(Long id) {
        log.info("Eliminando producto con ID: {}", id);

        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }

        productRepository.deleteById(id);

        // Invalidate cache
        invalidateCache(id);

        log.info("Producto eliminado satisfactoriamente: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchByName(String name) {
        log.debug("Buscando productos por nombre: {}", name);
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchByCategory(String category) {
        log.debug("Buscando productos por categoría: {}", category);
        return productRepository.findByCategoryAndActiveTrue(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Buscando productos por rango de precio: {} - {}", minPrice, maxPrice);
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAvailableProducts() {
        log.debug("Obteniendo productos disponibles");
        return productRepository.findAvailableProducts();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkStock(Long productId, Integer quantity) {
        log.debug("Verificando stock para producto {}: cantidad {}", productId, quantity);
        Product product = getProductById(productId);
        return product.getStock() >= quantity;
    }

    @Override
    public void decreaseStock(Long productId, Integer quantity) {
        log.info("Disminuyendo stock para producto {}: cantidad {}", productId, quantity);

        Product product = getProductById(productId);

        if (product.getStock() < quantity) {
            throw new InsufficientStockException(productId, quantity, product.getStock());
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);

        // Invalidate cache (data changed)
        invalidateCache(productId);

        log.info("Stock disminuido satisfactoriamente para producto {}", productId);
    }

    @Override
    public void increaseStock(Long productId, Integer quantity) {
        log.info("Aumentando stock para producto {}: cantidad {}", productId, quantity);

        Product product = getProductById(productId);
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);

        // Invalidate cache (data changed)
        invalidateCache(productId);

        log.info("Stock aumentado satisfactoriamente para producto {}", productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts(Integer threshold) {
        log.debug("Obteniendo productos con stock debajo de: {}", threshold);
        return productRepository.findLowStockProducts(threshold);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByCategory(String category) {
        log.debug("Contando productos por categoría: {}", category);
        return productRepository.countByCategory(category);
    }

    /**
     * Cache a product with TTL
     * Cachear un producto con TTL
     */
    private void cacheProduct(Product product) {
        String cacheKey = CACHE_KEY_PREFIX + product.getId();
        productRedisTemplate.opsForValue().set(cacheKey, product, CACHE_TTL_HOURS, TimeUnit.HOURS);
        log.debug("Producto cacheado: {}", product.getId());
    }

    /**
     * Invalidate product cache
     * Invalidar caché del producto
     */
    private void invalidateCache(Long productId) {
        String cacheKey = CACHE_KEY_PREFIX + productId;
        productRedisTemplate.delete(cacheKey);
        log.debug("Caché invalidado para producto: {}", productId);
    }
}
