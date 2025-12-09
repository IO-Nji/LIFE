package io.life.masterdata.controller;

import io.life.masterdata.dto.ProductVariantDto;
import io.life.masterdata.entity.ProductVariant;
import io.life.masterdata.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for ProductVariant management.
 * Allows admins to view, create, update, and delete product variants.
 */
@RestController
@RequestMapping("/api/masterdata/product-variants")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
public class ProductVariantController {

    private final ProductVariantService productVariantService;

    /**
     * GET /api/masterdata/product-variants
     * Get all product variants
     */
    @GetMapping
    public ResponseEntity<List<ProductVariantDto>> getAllProductVariants() {
        log.debug("Fetching all product variants");
        List<ProductVariantDto> variants = productVariantService.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(variants);
    }

    /**
     * GET /api/masterdata/product-variants/{id}
     * Get a specific product variant by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductVariantDto> getProductVariantById(@PathVariable Long id) {
        log.debug("Fetching product variant with ID: {}", id);
        return productVariantService.findById(id)
                .map(variant -> ResponseEntity.ok(convertToDto(variant)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /api/masterdata/product-variants
     * Create a new product variant
     * Request body: { "name", "description", "price", "estimatedTimeMinutes" }
     */
    @PostMapping
    public ResponseEntity<ProductVariantDto> createProductVariant(@RequestBody ProductVariantDto dto) {
        log.info("Creating new product variant: {}", dto.getName());
        
        ProductVariant variant = new ProductVariant();
        variant.setName(dto.getName());
        variant.setDescription(dto.getDescription());
        variant.setPrice(dto.getPrice());
        variant.setEstimatedTimeMinutes(dto.getEstimatedTimeMinutes());

        try {
            ProductVariant saved = productVariantService.save(variant);
            log.info("Product variant created successfully with ID: {}", saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(saved));
        } catch (Exception e) {
            log.error("Failed to create product variant: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * PUT /api/masterdata/product-variants/{id}
     * Update an existing product variant
     * Request body: { "name", "description", "price", "estimatedTimeMinutes" }
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductVariantDto> updateProductVariant(
            @PathVariable Long id,
            @RequestBody ProductVariantDto dto) {
        log.info("Updating product variant with ID: {}", id);

        var optionalVariant = productVariantService.findById(id);
        if (optionalVariant.isEmpty()) {
            log.warn("Product variant not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        ProductVariant variant = optionalVariant.get();
        variant.setName(dto.getName());
        variant.setDescription(dto.getDescription());
        variant.setPrice(dto.getPrice());
        variant.setEstimatedTimeMinutes(dto.getEstimatedTimeMinutes());

        try {
            ProductVariant updated = productVariantService.save(variant);
            log.info("Product variant updated successfully with ID: {}", id);
            return ResponseEntity.ok(convertToDto(updated));
        } catch (Exception e) {
            log.error("Failed to update product variant: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * DELETE /api/masterdata/product-variants/{id}
     * Delete a product variant
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductVariant(@PathVariable Long id) {
        log.info("Deleting product variant with ID: {}", id);

        if (productVariantService.findById(id).isPresent()) {
            try {
                productVariantService.deleteById(id);
                log.info("Product variant deleted successfully with ID: {}", id);
                return ResponseEntity.noContent().build();
            } catch (Exception e) {
                log.error("Failed to delete product variant: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } else {
            log.warn("Product variant not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Helper method to convert ProductVariant entity to DTO
     */
    private ProductVariantDto convertToDto(ProductVariant variant) {
        return new ProductVariantDto(
                variant.getId(),
                variant.getName(),
                variant.getDescription(),
                variant.getPrice(),
                variant.getEstimatedTimeMinutes()
        );
    }
}
