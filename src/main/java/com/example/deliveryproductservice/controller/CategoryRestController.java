
package com.example.deliveryproductservice.controller;
import com.example.deliveryproductservice.annotation.CurrentUser;
import com.example.deliveryproductservice.dto.category.CategoryBaseProjection;
import com.example.deliveryproductservice.dto.category.CategoryResponseDto;
import com.example.deliveryproductservice.dto.category.CreateCategoryDto;
import com.example.deliveryproductservice.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST контроллер для управления категориями товаров
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryRestController {

    private final CategoryService categoryService;
    /**
     * Получить все активные категории
     * GET /api/categories
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllActiveCategories() {
        log.info("📋 GET /api/categories - Getting all active categories");

        List<CategoryResponseDto> categories = categoryService.getAllActiveCategories();

        log.info("✅ Found {} active categories", categories.size());
        return ResponseEntity.ok(categories);
    }

    /**
     * Получить краткий список активных категорий (для dropdown/селекторов)
     * GET /api/categories/brief
     */
    @GetMapping("/brief")
    public ResponseEntity<List<CategoryBaseProjection>> getActiveCategoriesBrief() {
        log.info("📊 GET /api/categories/brief - Getting brief categories");

        List<CategoryBaseProjection> categories = categoryService.getActiveCategoriesBrief();

        log.info("✅ Found {} brief categories", categories.size());
        return ResponseEntity.ok(categories);
    }

    /**
     * Получить краткую информацию о категориях по списку ID
     * POST /api/categories/brief/by-ids
     */
    @PostMapping("/brief/by-ids")
    public ResponseEntity<List<CategoryBaseProjection>> getCategoriesBriefByIds(
            @RequestBody List<Long> ids) {

        log.info("🔍 POST /api/categories/brief/by-ids - Getting brief for IDs: {}", ids);

        if (ids == null || ids.isEmpty()) {
            log.warn("❌ Empty IDs list provided");
            return ResponseEntity.badRequest().build();
        }

        List<CategoryBaseProjection> categories = categoryService.getCategoriesBriefByIds(ids);

        log.info("✅ Found {} categories for provided IDs", categories.size());
        return ResponseEntity.ok(categories);
    }

    // ================================
    // 🔍 ПОЛУЧЕНИЕ ОТДЕЛЬНЫХ КАТЕГОРИЙ
    // ================================

    /**
     * Получить категорию по ID (полная информация)
     * GET /api/categories/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable Long id) {
        log.info("🔍 GET /api/categories/{} - Getting category by ID", id);

        try {
            CategoryResponseDto category = categoryService.getCategoryById(id);
            log.info("✅ Category found: {}", category.getName());
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            log.warn("❌ Category not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Получить краткую информацию о категории по ID
     * GET /api/categories/{id}/brief
     */
    @GetMapping("/{id}/brief")
    public ResponseEntity<CategoryBaseProjection> getCategoryBrief(@PathVariable Long id) {
        log.info("📊 GET /api/categories/{}/brief - Getting brief category", id);

        Optional<CategoryBaseProjection> category = categoryService.getCategoryBrief(id);

        if (category.isPresent()) {
            log.info("✅ Brief category found: {}", category.get().getName());
            return ResponseEntity.ok(category.get());
        } else {
            log.warn("❌ Category not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    // ================================
    // ✏️ СОЗДАНИЕ И ОБНОВЛЕНИЕ КАТЕГОРИЙ (ADMIN ONLY)
    // ================================

    /**
     * Создать новую категорию (с возможностью загрузки изображения)
     * POST /api/categories
     * Требует: роль ADMIN
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CategoryResponseDto> createCategory(
            @Valid @ModelAttribute CreateCategoryDto createCategoryDto,
            @CurrentUser Long userId,
            HttpServletRequest request) {

        log.info("➕ POST /api/categories - Creating new category: {}", createCategoryDto.getName());

        // Проверяем авторизацию
        String userEmail = request.getHeader("X-User-Email");
        String userRole = request.getHeader("X-User-Role");

        log.info("🔐 Create category request from User: ID={}, Email={}, Role={}",
                userId, userEmail, userRole);

        if (!"ROLE_ADMIN".equals(userRole)) {
            log.warn("❌ Access denied for user {} with role {}", userId, userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            CategoryResponseDto createdCategory = categoryService.createCategory(createCategoryDto, userId);

            log.info("✅ Category created successfully: {} by user {}",
                    createdCategory.getName(), userId);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);

        } catch (Exception e) {
            log.error("❌ Error creating category: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Обновить существующую категорию
     * PUT /api/categories/{id}
     * Требует: роль ADMIN
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CategoryResponseDto> updateCategory(
            @PathVariable Long id,
            @Valid @ModelAttribute CreateCategoryDto updateCategoryDto,
            @CurrentUser Long userId,
            HttpServletRequest request) {

        log.info("✏️ PUT /api/categories/{} - Updating category", id);

        // Проверяем авторизацию
        String userRole = request.getHeader("X-User-Role");

        if (!"ROLE_ADMIN".equals(userRole)) {
            log.warn("❌ Access denied for user {} with role {}", userId, userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            CategoryResponseDto updatedCategory = categoryService.updateCategory(id, updateCategoryDto, userId);

            log.info("✅ Category updated successfully: {}", updatedCategory.getName());
            return ResponseEntity.ok(updatedCategory);

        } catch (RuntimeException e) {
            log.warn("❌ Category not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("❌ Error updating category {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ================================
    // 🗑️ УДАЛЕНИЕ КАТЕГОРИЙ (ADMIN ONLY)
    // ================================

    /**
     * Удалить категорию (мягкое удаление - деактивация)
     * DELETE /api/categories/{id}
     * Требует: роль ADMIN
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id,
            @CurrentUser Long userId,
            HttpServletRequest request) {

        log.info("🗑️ DELETE /api/categories/{} - Deleting category", id);

        // Проверяем авторизацию
        String userRole = request.getHeader("X-User-Role");

        if (!"ROLE_ADMIN".equals(userRole)) {
            log.warn("❌ Access denied for user {} with role {}", userId, userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            categoryService.deleteCategory(id, userId);

            log.info("✅ Category {} deleted successfully by user {}", id, userId);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            log.warn("❌ Category not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("❌ Error deleting category {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ================================
    // 🛠️ ДОПОЛНИТЕЛЬНЫЕ ENDPOINT'Ы
    // ================================

    /**
     * Получить количество активных категорий
     * GET /api/categories/count
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getActiveCategoriesCount() {
        log.info("📊 GET /api/categories/count - Getting categories count");

        List<CategoryBaseProjection> categories = categoryService.getActiveCategoriesBrief();
        long count = categories.size();

        log.info("✅ Active categories count: {}", count);
        return ResponseEntity.ok(count);
    }

    /**
     * Проверка работоспособности сервиса категорий
     * GET /api/categories/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.debug("🔧 GET /api/categories/health - Health check");
        return ResponseEntity.ok("Categories service is up and running! 🎉");
    }

    /**
     * Получить метаинформацию о сервисе категорий
     * GET /api/categories/info
     */
    @GetMapping("/info")
    public ResponseEntity<CategoryServiceInfo> getServiceInfo() {
        log.debug("ℹ️ GET /api/categories/info - Getting service info");

        List<CategoryBaseProjection> categories = categoryService.getActiveCategoriesBrief();

        CategoryServiceInfo info = new CategoryServiceInfo(
                "Categories Service",
                "1.0.0",
                categories.size(),
                "Active"
        );

        return ResponseEntity.ok(info);
    }

    // ================================
    // 📦 ВСПОМОГАТЕЛЬНЫЕ КЛАССЫ
    // ================================

    /**
     * Информация о сервисе категорий
     */
    @Getter
    public static class CategoryServiceInfo {
        // Getters
        private final String serviceName;
        private final String version;
        private final long activeCategoriesCount;
        private final String status;

        public CategoryServiceInfo(String serviceName, String version, long activeCategoriesCount, String status) {
            this.serviceName = serviceName;
            this.version = version;
            this.activeCategoriesCount = activeCategoriesCount;
            this.status = status;
        }

    }
}