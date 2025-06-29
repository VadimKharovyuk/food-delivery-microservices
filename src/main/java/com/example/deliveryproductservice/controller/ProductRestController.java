package com.example.deliveryproductservice.controller;

import com.example.deliveryproductservice.annotation.CurrentUser;
import com.example.deliveryproductservice.dto.ProductDto.*;
import com.example.deliveryproductservice.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductRestController {

    private final ProductService productService;


    /**
     * Получить все доступные продукты
     * GET /api/products
     */
    @GetMapping
    public ResponseEntity<ProductResponseWrapper> getAllAvailableProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("📋 GET /api/products - Getting all available products: page={}, size={}", page, size);

        ProductResponseWrapper response = productService.getAllAvailableProducts(page, size);

        log.info("✅ Found {} products, hasNext: {}", response.getTotalCount(), response.getHasNext());
        return ResponseEntity.ok(response);
    }

    /**
     * Получить продукты конкретного магазина
     * GET /api/products/store/{storeId}
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<ProductResponseWrapper> getProductsByStore(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("🏪 GET /api/products/store/{} - Getting products for store: page={}, size={}", storeId, page, size);

        ProductResponseWrapper response = productService.getProductsByStore(storeId, page, size);

        log.info("✅ Found {} products for store {}, hasNext: {}", response.getTotalCount(), storeId, response.getHasNext());
        return ResponseEntity.ok(response);
    }

    /**
     * Получить продукты конкретной категории
     * GET /api/products/category/{categoryId}
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ProductResponseWrapper> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("📂 GET /api/products/category/{} - Getting products for category: page={}, size={}", categoryId, page, size);

        ProductResponseWrapper response = productService.getProductsByCategory(categoryId, page, size);

        log.info("✅ Found {} products for category {}, hasNext: {}", response.getTotalCount(), categoryId, response.getHasNext());
        return ResponseEntity.ok(response);
    }

    /**
     * Поиск продуктов по названию
     * GET /api/products/search?name={name}
     */
    @GetMapping("/search")
    public ResponseEntity<ProductResponseWrapper> searchProductsByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("🔍 GET /api/products/search - Searching products by name: '{}', page={}, size={}", name, page, size);

        if (name == null || name.trim().isEmpty()) {
            log.warn("❌ Empty search query provided");
            ProductResponseWrapper errorResponse = ProductResponseWrapper.error("Search query cannot be empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        ProductResponseWrapper response = productService.searchProductsByName(name.trim(), page, size);

        log.info("✅ Found {} products matching '{}', hasNext: {}", response.getTotalCount(), name, response.getHasNext());
        return ResponseEntity.ok(response);
    }

    /**
     * Получить краткую информацию о продуктах магазина (для быстрой загрузки)
     * GET /api/products/store/{storeId}/brief
     */
    @GetMapping("/store/{storeId}/brief")
    public ResponseEntity<ProductBriefResponseWrapper> getProductsBriefByStore(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("📊 GET /api/products/store/{}/brief - Getting brief products for store: page={}, size={}", storeId, page, size);

        ProductBriefResponseWrapper response = productService.getProductsBriefByStore(storeId, page, size);

        log.info("✅ Found {} brief products for store {}, hasNext: {}", response.getTotalCount(), storeId, response.getHasNext());
        return ResponseEntity.ok(response);
    }

    // ================================
    // 🔍 ПОЛУЧЕНИЕ ОТДЕЛЬНЫХ ПРОДУКТОВ
    // ================================

    /**
     * Получить продукт по ID
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SingleProductResponseWrapper> getProductById(@PathVariable Long id) {
        log.info("🔍 GET /api/products/{} - Getting product by ID", id);

        SingleProductResponseWrapper response = productService.getProductById(id);

        if (response.getSuccess()) {
            log.info("✅ Product found: {}", response.getProduct().getName());
            return ResponseEntity.ok(response);
        } else {
            log.warn("❌ Product not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // ================================
    // ✏️ СОЗДАНИЕ И ОБНОВЛЕНИЕ ПРОДУКТОВ (BUSINESS ONLY)
    // ================================

    /**
     * Создать новый продукт (с возможностью загрузки изображения)
     * POST /api/products
     * Требует: роль BUSINESS
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SingleProductResponseWrapper> createProduct(
            @Valid @ModelAttribute CreateProductDto createProductDto,
            @CurrentUser Long userId,
            HttpServletRequest request) {

        log.info("➕ POST /api/products - Creating new product: {} for store: {}", createProductDto.getName(), createProductDto.getStoreId());

        // Проверяем авторизацию
        String userEmail = request.getHeader("X-User-Email");
        String userRole = request.getHeader("X-User-Role");

        log.info("🔐 Create product request from User: ID={}, Email={}, Role={}", userId, userEmail, userRole);

        if (!"ROLE_BUSINESS".equals(userRole)) {
            log.warn("❌ Access denied for user {} with role {}. Required: ROLE_BUSINESS", userId, userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            SingleProductResponseWrapper response = productService.createProduct(createProductDto, userId);

            log.info("✅ Product created successfully: {} (ID: {}) by user {}",
                    response.getProduct().getName(), response.getProduct().getId(), userId);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            log.error("❌ Business error creating product: {}", e.getMessage());

            SingleProductResponseWrapper errorResponse = SingleProductResponseWrapper.builder()
                    .product(null)
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);

        } catch (Exception e) {
            log.error("❌ Unexpected error creating product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Создать новый продукт (JSON версия без изображения)
     * POST /api/products/simple
     * Требует: роль BUSINESS
     */
    @PostMapping("/simple")
    public ResponseEntity<SingleProductResponseWrapper> createProductSimple(
            @Valid @RequestBody CreateProductDto createProductDto,
            @CurrentUser Long userId,
            HttpServletRequest request) {

        log.info("🛍️ POST /api/products/simple - Creating new product: {} for store: {}", createProductDto.getName(), createProductDto.getStoreId());

        // Проверяем авторизацию
        String userRole = request.getHeader("X-User-Role");

        if (!"ROLE_BUSINESS".equals(userRole)) {
            log.warn("❌ Access denied for user {} with role {}. Required: ROLE_BUSINESS", userId, userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            SingleProductResponseWrapper response = productService.createProduct(createProductDto, userId);

            log.info("✅ Product created successfully: {} (ID: {}) by user {}",
                    response.getProduct().getName(), response.getProduct().getId(), userId);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("❌ Error creating product: {}", e.getMessage(), e);

            SingleProductResponseWrapper errorResponse = SingleProductResponseWrapper.builder()
                    .product(null)
                    .success(false)
                    .message("Failed to create product: " + e.getMessage())
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Обновить существующий продукт
     * PUT /api/products/{id}
     * Требует: роль BUSINESS
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SingleProductResponseWrapper> updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute CreateProductDto updateProductDto,
            @CurrentUser Long userId,
            HttpServletRequest request) {

        log.info("✏️ PUT /api/products/{} - Updating product", id);

        // Проверяем авторизацию
        String userRole = request.getHeader("X-User-Role");

        if (!"ROLE_BUSINESS".equals(userRole)) {
            log.warn("❌ Access denied for user {} with role {}", userId, userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            SingleProductResponseWrapper response = productService.updateProduct(id, updateProductDto, userId);

            if (response.getSuccess()) {
                log.info("✅ Product updated successfully: {}", response.getProduct().getName());
                return ResponseEntity.ok(response);
            } else {
                log.warn("❌ Product not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            log.error("❌ Error updating product {}: {}", id, e.getMessage(), e);

            SingleProductResponseWrapper errorResponse = SingleProductResponseWrapper.builder()
                    .product(null)
                    .success(false)
                    .message("Failed to update product: " + e.getMessage())
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ================================
    // 🗑️ УДАЛЕНИЕ ПРОДУКТОВ (BUSINESS ONLY)
    // ================================

    /**
     * Удалить продукт (мягкое удаление)
     * DELETE /api/products/{id}
     * Требует: роль BUSINESS
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @CurrentUser Long userId,
            HttpServletRequest request) {

        log.info("🗑️ DELETE /api/products/{} - Deleting product", id);

        // Проверяем авторизацию
        String userRole = request.getHeader("X-User-Role");

        if (!"ROLE_BUSINESS".equals(userRole)) {
            log.warn("❌ Access denied for user {} with role {}", userId, userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            productService.deleteProduct(id, userId);

            log.info("✅ Product {} deleted successfully by user {}", id, userId);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            log.warn("❌ Product not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("❌ Error deleting product {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Жесткое удаление продукта (только для админов)
     * DELETE /api/products/{id}/hard
     * Требует: роль ADMIN
     */
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeleteProduct(
            @PathVariable Long id,
            @CurrentUser Long userId,
            HttpServletRequest request) {

        log.info("💥 DELETE /api/products/{}/hard - Hard deleting product", id);

        // Проверяем авторизацию
        String userRole = request.getHeader("X-User-Role");

        if (!"ROLE_ADMIN".equals(userRole)) {
            log.warn("❌ Access denied for user {} with role {}. Required: ROLE_ADMIN", userId, userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            productService.hardDeleteProduct(id, userId);

            log.info("✅ Product {} hard deleted successfully by user {}", id, userId);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            log.warn("❌ Product not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("❌ Error hard deleting product {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ================================
    // 🛠️ ДОПОЛНИТЕЛЬНЫЕ ENDPOINT'Ы
    // ================================

    /**
     * Проверка работоспособности сервиса продуктов
     * GET /api/products/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.debug("🔧 GET /api/products/health - Health check");
        return ResponseEntity.ok("Products service is up and running! 🛍️");
    }
}