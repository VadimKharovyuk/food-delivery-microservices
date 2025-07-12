package com.example.deliveryproductservice.controller;
import com.example.deliveryproductservice.dto.category.*;
import com.example.deliveryproductservice.repository.CategoryRepository;
import com.example.deliveryproductservice.service.CategoryService;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryRestController {


    private final CategoryService categoryService;


    /**
     * Получить краткую информацию о категории по ID
     * GET /api/categories/{id}/brief
     */
    @GetMapping("/{id}/brief")
    public ResponseEntity<ApiResponse<CategoryBaseProjection>> getCategoryBrief(@PathVariable Long id) {
        log.info("📊 GET /api/categories/{}/brief - Getting brief category", id);

        ApiResponse<CategoryBaseProjection> response = categoryService.getCategoryBrief(id);

        if (response.isSuccess()) {
            log.info("✅ Brief category found: {}", response.getData().getName());
            return ResponseEntity.ok(response);
        } else if (response.getData() == null && !response.isSuccess()) {
            log.warn("❌ Category not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Получить все активные категории (полная информация)
     * GET /api/categories
     */
    @GetMapping
    public ResponseEntity<ListApiResponse<CategoryResponseDto>> getAllActiveCategories() {

        ListApiResponse<CategoryResponseDto> response = categoryService.getAllActiveCategories();

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            log.error("❌ Error getting active categories: {}", response.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Получить все категории включая неактивные (полная информация)
     * GET /api/categories/all
     * Требует: роль ADMIN
     */
    @GetMapping("/all")
    public ResponseEntity<ListApiResponse<CategoryResponseDto>> getAllCategories(
            HttpServletRequest request) {

        String userRole = request.getHeader("X-User-Role");
        if (!"ROLE_ADMIN".equals(userRole)) {
            log.warn("❌ Access denied for role: {}", userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ListApiResponse.error("Доступ запрещен"));
        }
        ListApiResponse<CategoryResponseDto> response = categoryService.getAllCategories();

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Поиск категорий по названию (полная информация)
     * GET /api/categories/search?name=pizza
     */
    @GetMapping("/search")
    public ResponseEntity<ListApiResponse<CategoryResponseDto>> searchCategories(
            @RequestParam String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ListApiResponse.error("Параметр поиска не может быть пустым"));
        }
        ListApiResponse<CategoryResponseDto> response = categoryService.searchCategories(name.trim());

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ================================
    // 📊 ПОЛУЧЕНИЕ СПИСКОВ - КРАТКАЯ ИНФОРМАЦИЯ (ПРОЕКЦИИ)
    // ================================

    /**
     * Получить краткий список активных категорий (для dropdown/селекторов)
     * GET /api/categories/brief
     */
    @GetMapping("/brief")
    public ResponseEntity<ListApiResponse<CategoryBaseProjection>> getActiveCategoriesBrief() {
        try {
            ListApiResponse<CategoryBaseProjection> response = categoryService.getActiveCategoriesBrief();

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            log.error("Ошибка при получении списка категорий", e);
            // Возвращаем ошибку
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить краткий список всех категорий (включая неактивные)
     * GET /api/categories/brief/all
     * Требует: роль ADMIN
     */
    @GetMapping("/brief/all")
    public ResponseEntity<ListApiResponse<CategoryBaseProjection>> getAllCategoriesBrief(
            HttpServletRequest request) {
        String userRole = request.getHeader("X-User-Role");
        if (!"ROLE_ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ListApiResponse.error("Доступ запрещен"));
        }
        ListApiResponse<CategoryBaseProjection> response = categoryService.getAllCategoriesBrief();

        if (response.isSuccess()) {
            log.info("✅ Found {} total brief categories", response.getTotalCount());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Поиск кратких данных категорий по названию
     * GET /api/categories/brief/search?name=pizza
     */
    @GetMapping("/brief/search")
    public ResponseEntity<ListApiResponse<CategoryBaseProjection>> searchCategoriesBrief(
            @RequestParam String name) {

        log.info("🔍 GET /api/categories/brief/search - Searching brief categories by name: {}", name);

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ListApiResponse.error("Параметр поиска не может быть пустым"));
        }

        ListApiResponse<CategoryBaseProjection> response = categoryService.searchCategoriesBrief(name.trim());

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Получить краткие данные категорий по списку ID
     * POST /api/categories/brief/by-ids
     */
    @PostMapping("/brief/by-ids")
    public ResponseEntity<ListApiResponse<CategoryBaseProjection>> getCategoriesBriefByIds(
            @RequestBody List<Long> ids) {

        log.info("📊 POST /api/categories/brief/by-ids - Getting categories by IDs: {}", ids);

        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ListApiResponse.error("Список ID не может быть пустым"));
        }

        ListApiResponse<CategoryBaseProjection> response = categoryService.getCategoriesBriefByIds(ids);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ================================
    // 🔍 ПОЛУЧЕНИЕ ОТДЕЛЬНЫХ КАТЕГОРИЙ
    // ================================

    /**
     * Получить категорию по ID (полная информация)
     * GET /api/categories/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoryById(@PathVariable Long id) {
        log.info("🔍 GET /api/categories/{} - Getting category by ID", id);

        ApiResponse<CategoryResponseDto> response = categoryService.getCategoryById(id);

        if (response.isSuccess()) {
            log.info("✅ Category found: {}", response.getData().getName());
            return ResponseEntity.ok(response);
        } else if (response.getData() == null && !response.isSuccess()) {
            log.warn("❌ Category not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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
    public ResponseEntity<ApiResponse<CategoryResponseDto>> createCategory(
            @Valid @ModelAttribute CreateCategoryDto createCategoryDto,
            @RequestHeader("X-User-Id") Long userId,
            HttpServletRequest request) {

        log.info("➕ POST /api/categories - Creating new category: {}", createCategoryDto.getName());

        // Проверяем авторизацию
        String userEmail = request.getHeader("X-User-Email");
        String userRole = request.getHeader("X-User-Role");

        log.info("🔐 Create category request from User: ID={}, Email={}, Role={}",
                userId, userEmail, userRole);

        if (!"ROLE_ADMIN".equals(userRole)) {
            log.warn("❌ Access denied for user {} with role {}", userId, userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Доступ запрещен"));
        }


        ApiResponse<CategoryResponseDto> response = categoryService.createCategory(createCategoryDto, userId);

        if (response.isSuccess()) {
            log.info("✅ Category created successfully: {} by user {}",
                    response.getData().getName(), userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            log.error("❌ Error creating category: {}", response.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Обновить существующую категорию
     * PUT /api/categories/{id}
     * Требует: роль ADMIN
     */
    @PostMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CategoryResponseDto>> updateCategory(
            @PathVariable Long id,
            @Valid @ModelAttribute CreateCategoryDto updateCategoryDto,
            @RequestHeader("X-User-Id") Long userId,
            HttpServletRequest request) {

        log.info("✏️ PUT /api/categories/{} - Updating category", id);

        // Проверяем авторизацию
        String userRole = request.getHeader("X-User-Role");

        if (!"ROLE_ADMIN".equals(userRole)) {
            log.warn("❌ Access denied for user {} with role {}", userId, userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Доступ запрещен"));
        }

        ApiResponse<CategoryResponseDto> response = categoryService.updateCategory(id, updateCategoryDto, userId);

        if (response.isSuccess()) {
            log.info("✅ Category updated successfully: {}", response.getData().getName());
            return ResponseEntity.ok(response);
        } else if (response.getData() == null && !response.isSuccess()) {
            log.warn("❌ Category not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ================================
    // 🗑️ УДАЛЕНИЕ КАТЕГОРИЙ (ADMIN ONLY)
    // ================================

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            HttpServletRequest request) {

        log.info("🗑️ DELETE /api/categories/{} - Deleting category", id);

        // Проверяем авторизацию
        String userRole = request.getHeader("X-User-Role");

        if (!"ROLE_ADMIN".equals(userRole)) {
            log.warn("❌ Access denied for user {} with role {}", userId, userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Доступ запрещен"));
        }

        ApiResponse<Void> response = categoryService.deleteCategory(id, userId);

        if (response.isSuccess()) {
            log.info("✅ Category {} deleted successfully by user {}", id, userId);
            return ResponseEntity.ok(response);
        } else if (!response.isSuccess() && response.getMessage().contains("не найдена")) {
            log.warn("❌ Category not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> toggleCategoryStatus(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            HttpServletRequest request) {

        log.info("🔄 PATCH /api/categories/{}/toggle - Toggling category status by user: {}", id, userId);

        // Проверяем авторизацию
        String userRole = request.getHeader("X-User-Role");

        if (!"ROLE_ADMIN".equals(userRole)) {
            log.warn("❌ Access denied for user {} with role {}", userId, userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Доступ запрещен"));
        }

        try {
            ApiResponse<CategoryResponseDto> response = categoryService.toggleCategoryStatus(id, userId);

            if (response.isSuccess()) {
                log.info("✅ Category status toggled for ID: {} by user: {}", id, userId);
                return ResponseEntity.ok(response);
            } else if (response.getMessage() != null && response.getMessage().contains("не найдена")) {
                log.warn("❌ Category ID: {} not found", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else {
                log.warn("❌ Failed to toggle category status: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            log.error("💥 Error toggling category status for ID: {} by user: {}", id, userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Внутренняя ошибка при изменении статуса категории"));
        }
    }

    // ================================
    // 📊 СТАТИСТИКА И АНАЛИТИКА
    // ================================

    /**
     * Получить статистику категорий
     * GET /api/categories/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ListApiResponse<CategoryRepository.CategoryStatsProjection>> getCategoryStats() {
        log.info("📊 GET /api/categories/stats - Getting category statistics");

        ListApiResponse<CategoryRepository.CategoryStatsProjection> response = categoryService.getCategoryStats();

        if (response.isSuccess()) {
            log.info("✅ Retrieved category statistics with {} entries", response.getTotalCount());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



    // ================================
    // 🛠️ ВСПОМОГАТЕЛЬНЫЕ ENDPOINT'Ы
    // ================================

    /**
     * Получить количество активных категорий
     * GET /api/categories/count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getActiveCategoriesCount() {
        log.info("📊 GET /api/categories/count - Getting categories count");

        try {
            Long count = categoryService.getActiveCategoriesCount();
            log.info("✅ Active categories count: {}", count);
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            log.error("❌ Error getting categories count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Ошибка получения количества категорий"));
        }
    }

    /**
     * Проверить существование категории по имени
     * GET /api/categories/exists?name=pizza
     */
    @GetMapping("/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkCategoryExists(@RequestParam String name) {
        log.info("🔍 GET /api/categories/exists - Checking if category exists: {}", name);

        try {
            boolean exists = categoryService.existsActiveCategoryByName(name);
            log.info("✅ Category '{}' exists: {}", name, exists);
            return ResponseEntity.ok(ApiResponse.success(exists));
        } catch (Exception e) {
            log.error("❌ Error checking category existence", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Ошибка проверки существования категории"));
        }
    }


    /**
     * Проверка работоспособности сервиса категорий
     * GET /api/categories/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        log.debug("🔧 GET /api/categories/health - Health check");
        return ResponseEntity.ok(ApiResponse.success("Categories service is up and running! 🎉"));
    }
}