package com.edidev.academyApp.controller;

import com.edidev.academyApp.dto.ProductCategoryDTO;
import com.edidev.academyApp.dto.ProductDTO;
import com.edidev.academyApp.dto.ProductVariantDTO;
import com.edidev.academyApp.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "APIs del catálogo de productos de la academia")
public class ProductController {

    private final ProductService productService;

    // ===== CATEGORÍAS =====

    @GetMapping("/categories")
    @Operation(summary = "Obtener categorías activas del catálogo")
    public ResponseEntity<List<ProductCategoryDTO>> getCategories() {
        return ResponseEntity.ok(productService.getActiveCategories());
    }

    @GetMapping("/categories/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener todas las categorías (Admin)")
    public ResponseEntity<List<ProductCategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    // ===== PRODUCTOS =====

    @GetMapping("/products")
    @Operation(summary = "Obtener todos los productos activos")
    public ResponseEntity<List<ProductDTO>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean featured) {

        if (q != null && !q.isBlank()) {
            return ResponseEntity.ok(productService.searchProducts(q));
        }
        if (categorySlug != null) {
            return ResponseEntity.ok(productService.getProductsByCategorySlug(categorySlug));
        }
        if (categoryId != null) {
            return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
        }
        if (Boolean.TRUE.equals(featured)) {
            return ResponseEntity.ok(productService.getFeaturedProducts());
        }
        return ResponseEntity.ok(productService.getActiveProducts());
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Obtener producto por ID")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/products/slug/{slug}")
    @Operation(summary = "Obtener producto por slug")
    public ResponseEntity<ProductDTO> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getProductBySlug(slug));
    }

    // ===== ADMIN — PRODUCTOS =====

    @PostMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear producto (Admin)")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar producto (Admin)")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id,
                                                     @RequestBody ProductDTO request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @PatchMapping("/products/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar/desactivar producto (Admin)")
    public ResponseEntity<Void> toggleStatus(@PathVariable Long id) {
        productService.toggleProductStatus(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/products/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Subir imagen del producto (Admin)")
    public ResponseEntity<Map<String, String>> uploadProductImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        String imageUrl = productService.uploadProductImage(id, file);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    // ===== ADMIN — VARIANTES =====

    @PostMapping("/products/{productId}/variants")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Agregar variante a un producto (Admin)")
    public ResponseEntity<ProductVariantDTO> addVariant(
            @PathVariable Long productId,
            @RequestBody ProductVariantDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.addVariant(productId, dto));
    }

    @PutMapping("/products/{productId}/variants/{variantId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar variante (Admin)")
    public ResponseEntity<ProductVariantDTO> updateVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @RequestBody ProductVariantDTO dto) {
        return ResponseEntity.ok(productService.updateVariant(productId, variantId, dto));
    }

    @DeleteMapping("/products/{productId}/variants/{variantId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar variante (Admin)")
    public ResponseEntity<Void> deleteVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId) {
        productService.deleteVariant(productId, variantId);
        return ResponseEntity.noContent().build();
    }
}

