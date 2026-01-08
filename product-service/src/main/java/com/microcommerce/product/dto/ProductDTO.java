package com.microcommerce.product.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for Product
 * Objeto de Transferencia de Datos para Product
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    @NotBlank(message = "El nombre del producto es requerido")
    @Size(min = 3, max = 200, message = "El nombre del producto debe tener entre 3 y 200 caracteres")
    private String name;

    @Size(max = 1000, message = "La descripción no debe exceder 1000 caracteres")
    private String description;

    @NotNull(message = "El precio es requerido")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que 0")
    @DecimalMax(value = "999999.99", message = "El precio no debe exceder 999,999.99")
    private BigDecimal price;

    @NotNull(message = "El stock es requerido")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @NotBlank(message = "La categoría es requerida")
    @Size(max = 100, message = "La categoría no debe exceder 100 caracteres")
    private String category;

    @Size(max = 500, message = "La URL de la imagen no debe exceder 500 caracteres")
    private String imageUrl;

    private Boolean active = true;
}

