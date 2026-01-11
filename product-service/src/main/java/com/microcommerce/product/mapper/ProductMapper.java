package com.microcommerce.product.mapper;

import com.microcommerce.product.dto.ProductDTO;
import com.microcommerce.product.entity.Product;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Product entity and ProductDTO
 * Mapper para convertir entre entidad Product y ProductDTO
 */
@Component
public class ProductMapper {

    /**
     * Convert ProductDTO to Product entity
     * Convertir ProductDTO a entidad Product
     */
    public Product toEntity(ProductDTO dto) {
        if (dto == null) {
            return null;
        }

        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(dto.getCategory());
        product.setImageUrl(dto.getImageUrl());
        product.setActive(dto.getActive() != null ? dto.getActive() : true);

        return product;
    }

    /**
     * Update Product entity from ProductDTO
     * Actualizar entidad Product desde ProductDTO
     */
    public void updateEntityFromDto(ProductDTO dto, Product product) {
        if (dto == null || product == null) {
            return;
        }

        if (dto.getName() != null) {
            product.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice());
        }
        if (dto.getStock() != null) {
            product.setStock(dto.getStock());
        }
        if (dto.getCategory() != null) {
            product.setCategory(dto.getCategory());
        }
        if (dto.getImageUrl() != null) {
            product.setImageUrl(dto.getImageUrl());
        }
        if (dto.getActive() != null) {
            product.setActive(dto.getActive());
        }
    }

    /**
     * Convert Product entity to ProductDTO
     * Convertir entidad Product a ProductDTO
     */
    public ProductDTO toDto(Product product) {
        if (product == null) {
            return null;
        }

        ProductDTO dto = new ProductDTO();
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setCategory(product.getCategory());
        dto.setImageUrl(product.getImageUrl());
        dto.setActive(product.getActive());

        return dto;
    }
}
