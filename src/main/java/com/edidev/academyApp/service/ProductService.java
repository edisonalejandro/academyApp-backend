package com.edidev.academyApp.service;

import com.edidev.academyApp.dto.ProductCategoryDTO;
import com.edidev.academyApp.dto.ProductDTO;
import com.edidev.academyApp.dto.ProductVariantDTO;
import com.edidev.academyApp.exception.ResourceNotFoundException;
import com.edidev.academyApp.model.Product;
import com.edidev.academyApp.model.ProductCategory;
import com.edidev.academyApp.model.ProductVariant;
import com.edidev.academyApp.repository.ProductCategoryRepository;
import com.edidev.academyApp.repository.ProductRepository;
import com.edidev.academyApp.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductVariantRepository variantRepository;

    @Value("${app.upload.dir:/app/uploads}")
    private String uploadDir;

    // ===================== CATEGORÍAS =====================

    public List<ProductCategoryDTO> getActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderBySortOrderAsc()
                .stream().map(this::toCategoryDTO).collect(Collectors.toList());
    }

    public List<ProductCategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream().map(this::toCategoryDTO).collect(Collectors.toList());
    }

    // ===================== PRODUCTOS =====================

    public List<ProductDTO> getActiveProducts() {
        return productRepository.findByIsActiveTrueOrderByFeaturedDescNameAsc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ProductDTO> getFeaturedProducts() {
        return productRepository.findByFeaturedTrueAndIsActiveTrueOrderByNameAsc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndIsActiveTrueOrderByNameAsc(categoryId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ProductDTO> getProductsByCategorySlug(String slug) {
        ProductCategory category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + slug));
        return getProductsByCategory(category.getId());
    }

    public ProductDTO getProductById(Long id) {
        return toDTO(findById(id));
    }

    public ProductDTO getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + slug));
        return toDTO(product);
    }

    public List<ProductDTO> searchProducts(String term) {
        if (term == null || term.isBlank()) {
            return getActiveProducts();
        }
        return productRepository.searchActiveProducts(term.trim())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ===================== ADMIN — PRODUCTOS =====================

    @Transactional
    public ProductDTO createProduct(ProductDTO request) {
        ProductCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + request.getCategoryId()));

        Product product = Product.builder()
                .category(category)
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .imageUrl(request.getImageUrl())
                .isActive(Boolean.TRUE.equals(request.getIsActive()))
                .featured(Boolean.TRUE.equals(request.getFeatured()))
                .build();

        return toDTO(productRepository.save(product));
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO request) {
        Product product = findById(id);

        if (request.getCategoryId() != null) {
            ProductCategory cat = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
            product.setCategory(cat);
        }
        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getBasePrice() != null) product.setBasePrice(request.getBasePrice());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
        if (request.getIsActive() != null) product.setIsActive(request.getIsActive());
        if (request.getFeatured() != null) product.setFeatured(request.getFeatured());

        return toDTO(productRepository.save(product));
    }

    @Transactional
    public void toggleProductStatus(Long id) {
        Product product = findById(id);
        product.setIsActive(!product.getIsActive());
        productRepository.save(product);
    }

    /**
     * Sube una imagen para un producto. Valida tipo MIME, guarda en disco y actualiza imageUrl.
     * @return URL pública de la imagen
     */
    @Transactional
    public String uploadProductImage(Long productId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Solo se permiten archivos de imagen");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : ".jpg";
        // Sanitize extension
        ext = ext.toLowerCase().replaceAll("[^.a-z0-9]", "");
        String filename = "product-" + productId + "-" + UUID.randomUUID() + ext;

        try {
            Path dir = Paths.get(uploadDir, "products");
            Files.createDirectories(dir);
            Path dest = dir.resolve(filename);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Error guardando imagen: {}", e.getMessage());
            throw new RuntimeException("No se pudo guardar la imagen");
        }

        String imageUrl = "/api/catalog/uploads/" + filename;
        Product product = findById(productId);
        product.setImageUrl(imageUrl);
        productRepository.save(product);

        return imageUrl;
    }

    // ===================== ADMIN — VARIANTES =====================

    @Transactional
    public ProductVariantDTO addVariant(Long productId, ProductVariantDTO dto) {
        Product product = findById(productId);

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .size(dto.getSize())
                .color(dto.getColor())
                .sku(dto.getSku())
                .stock(dto.getStock() != null ? dto.getStock() : 0)
                .additionalPrice(dto.getAdditionalPrice() != null ? dto.getAdditionalPrice() : BigDecimal.ZERO)
                .isActive(true)
                .build();

        return toVariantDTO(variantRepository.save(variant));
    }

    @Transactional
    public ProductVariantDTO updateVariant(Long productId, Long variantId, ProductVariantDTO dto) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada: " + variantId));

        if (!variant.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("La variante no pertenece al producto indicado");
        }

        if (dto.getSize() != null) variant.setSize(dto.getSize());
        if (dto.getStock() != null) variant.setStock(dto.getStock());
        if (dto.getAdditionalPrice() != null) variant.setAdditionalPrice(dto.getAdditionalPrice());
        if (dto.getColor() != null) variant.setColor(dto.getColor());
        if (dto.getIsActive() != null) variant.setIsActive(dto.getIsActive());

        return toVariantDTO(variantRepository.save(variant));
    }

    @Transactional
    public void deleteVariant(Long productId, Long variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada: " + variantId));

        if (!variant.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("La variante no pertenece al producto indicado");
        }

        variant.setIsActive(false);
        variantRepository.save(variant);
    }

    // ===================== HELPERS =====================

    private Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
    }

    private ProductDTO toDTO(Product p) {
        return ProductDTO.builder()
                .id(p.getId())
                .categoryId(p.getCategory().getId())
                .categoryName(p.getCategory().getName())
                .categorySlug(p.getCategory().getSlug())
                .name(p.getName())
                .slug(p.getSlug())
                .description(p.getDescription())
                .basePrice(p.getBasePrice())
                .imageUrl(p.getImageUrl())
                .isActive(p.getIsActive())
                .featured(p.getFeatured())
                .variants(p.getVariants().stream()
                        .filter(ProductVariant::getIsActive)
                        .map(this::toVariantDTO)
                        .collect(Collectors.toList()))
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private ProductVariantDTO toVariantDTO(ProductVariant v) {
        return ProductVariantDTO.builder()
                .id(v.getId())
                .productId(v.getProduct().getId())
                .size(v.getSize())
                .color(v.getColor())
                .sku(v.getSku())
                .stock(v.getStock())
                .additionalPrice(v.getAdditionalPrice())
                .isActive(v.getIsActive())
                .createdAt(v.getCreatedAt())
                .build();
    }

    private ProductCategoryDTO toCategoryDTO(ProductCategory c) {
        return ProductCategoryDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .description(c.getDescription())
                .imageUrl(c.getImageUrl())
                .isActive(c.getIsActive())
                .sortOrder(c.getSortOrder())
                .productCount((int) c.getProducts().stream().filter(Product::getIsActive).count())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
