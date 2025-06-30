package com.example.deliveryproductservice.controller;
import com.example.deliveryproductservice.annotation.CurrentUser;
import com.example.deliveryproductservice.dto.StoreDto.*;
import com.example.deliveryproductservice.service.StoreService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@Slf4j
public class StoreController {

    private final StoreService storeService;


    @GetMapping
    public ResponseEntity<StoreResponseWrapper> getActiveStores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("📋 GET /api/stores - Getting active stores: page={}, size={}", page, size);
        StoreResponseWrapper response = storeService.getActiveStores(page, size);
        log.info("✅ Found {} stores, hasNext: {}", response.getTotalCount(), response.getHasNext());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/brief")
    public ResponseEntity<StoreBriefResponseWrapper> getActiveStoresBrief(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

        log.debug("Getting active stores brief with pagination: page={}, size={}", page, size);

        try {
            StoreBriefResponseWrapper response = storeService.getActiveStoresBrief(page, size);

            if (response.getSuccess()) {
                log.debug("Successfully retrieved {} stores for page {}", response.getTotalCount(), page);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Invalid pagination parameters: page={}, size={}", page, size, e);
            return ResponseEntity.badRequest()
                    .body(StoreBriefResponseWrapper.error("Неверные параметры пагинации"));
        } catch (Exception e) {
            log.error("Unexpected error getting brief stores", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(StoreBriefResponseWrapper.error("Внутренняя ошибка сервера"));
        }
    }

    @GetMapping("/ui")
    public ResponseEntity<StoreUIResponseWrapper> getStoresForUI() {
        log.debug("Getting stores for UI");
        StoreUIResponseWrapper response = storeService.getActiveStoresForUI();

        return response.getSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SingleStoreResponseWrapper> getStoreById(@PathVariable Long id) {
        log.info("🔍 GET /api/stores/{} - Getting store by ID", id);

        SingleStoreResponseWrapper response = storeService.getStoreById(id);

        if (response.getSuccess()) {
            log.info("✅ Store found: {}", response.getStore().getName());
            return ResponseEntity.ok(response);
        } else {
            log.warn("❌ Store not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<StoreResponseWrapper> getStoresByOwner(
            @PathVariable Long ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("📋 GET /api/stores/owner/{} - Getting stores for owner: page={}, size={}", ownerId, page, size);

        StoreResponseWrapper response = storeService.getStoresByOwner(ownerId, page, size);

        log.info("✅ Found {} stores for owner {}", response.getTotalCount(), ownerId);
        return ResponseEntity.ok(response);
    }

    // ================================
    // 👤 МЕТОДЫ С АВТОРИЗАЦИЕЙ (@CurrentUser)
    // ================================

    /**
     * Получить магазины текущего пользователя
     * GET /api/stores/my
     */
    @GetMapping("/my")
    public ResponseEntity<StoreResponseWrapper> getMyStores(
            @CurrentUser Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("📋 GET /api/stores/my - Getting stores for user: {} (page={}, size={})", userId, page, size);

        StoreResponseWrapper response = storeService.getStoresByOwner(userId, page, size);

        log.info("✅ Found {} stores for user {}", response.getTotalCount(), userId);
        return ResponseEntity.ok(response);
    }


    /**
     * 🏪 Создать новый магазин через JSON (без файлов)
     * POST /api/stores/json
     * Требует: роль ROLE_BUSINESS
     */
    @PostMapping(value = "/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StoreResponseDto> createStoreJson(
            @Valid @RequestBody CreateStoreDto createStoreDto,
            @CurrentUser Long userId,
            HttpServletRequest request) {

        log.info("🏪 POST /api/stores/json - Creating new store: {} by user: {}", createStoreDto.getName(), userId);

        // Логируем полученные данные
        log.info("📍 JSON Address: street={}, city={}, region={}, country={}",
                createStoreDto.getStreet(),
                createStoreDto.getCity(),
                createStoreDto.getRegion(),
                createStoreDto.getCountry());

        // 🔐 Проверяем роль ROLE_BUSINESS
        ResponseEntity<StoreResponseDto> authCheck = checkBusinessRole(userId, request);
        if (authCheck != null) {
            return authCheck;
        }

        try {
            StoreResponseDto createdStore = storeService.createStore(createStoreDto, userId);

            log.info("✅ Store created successfully: {} (ID: {})", createdStore.getName(), createdStore.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStore);

        } catch (RuntimeException e) {
            return handleStoreCreationError(e);
        } catch (Exception e) {
            log.error("❌ Unexpected error creating store", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🏪 Создать новый магазин (с изображением)
     * POST /api/stores
     * Требует: роль ROLE_BUSINESS
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoreResponseDto> createStore(
            @Valid @ModelAttribute CreateStoreDto createStoreDto,
            @CurrentUser Long userId,
            HttpServletRequest request) {

        log.info("🏪 POST /api/stores - Creating new store: {} by user: {}", createStoreDto.getName(), userId);

        // 🔐 Проверяем роль ROLE_BUSINESS
        ResponseEntity<StoreResponseDto> authCheck = checkBusinessRole(userId, request);
        if (authCheck != null) {
            return authCheck;
        }

        try {
            StoreResponseDto createdStore = storeService.createStore(createStoreDto, userId);

            log.info("✅ Store created successfully: {} (ID: {})", createdStore.getName(), createdStore.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStore);

        } catch (RuntimeException e) {
            return handleStoreCreationError(e);
        } catch (Exception e) {
            log.error("❌ Unexpected error creating store", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🏪 Создать новый магазин (JSON версия без изображения)
     * POST /api/stores/simple
     * Требует: роль ROLE_BUSINESS
     */
    @PostMapping("/simple")
    public ResponseEntity<StoreResponseDto> createStoreSimple(
            @Valid @RequestBody CreateStoreDto createStoreDto,
            @CurrentUser Long userId, // ← Используем @CurrentUser
            HttpServletRequest request) {

        log.info("🏪 POST /api/stores/simple - Creating new store: {} by user: {}", createStoreDto.getName(), userId);

        // 🔐 Проверяем роль ROLE_BUSINESS
        ResponseEntity<StoreResponseDto> authCheck = checkBusinessRole(userId, request);
        if (authCheck != null) {
            return authCheck;
        }

        try {
            StoreResponseDto createdStore = storeService.createStore(createStoreDto, userId);

            log.info("✅ Store created successfully: {} (ID: {})", createdStore.getName(), createdStore.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStore);

        } catch (RuntimeException e) {
            return handleStoreCreationError(e);
        } catch (Exception e) {
            log.error("❌ Unexpected error creating store", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ================================
    // 🔧 ПРИВАТНЫЕ МЕТОДЫ
    // ================================

    /**
     * 🔐 Проверить роль ROLE_BUSINESS
     */
    private ResponseEntity<StoreResponseDto> checkBusinessRole(Long userId, HttpServletRequest request) {
        String userRole = request.getHeader("X-User-Role");

        log.info("🔐 Business request from User: ID={}, Role={}", userId, userRole);

        if (!"ROLE_BUSINESS".equals(userRole)) {
            log.warn("❌ Access denied for user {} with role {}. Required: ROLE_BUSINESS", userId, userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return null; // Проверка прошла успешно
    }

    /**
     * 🚨 Обработка ошибок создания магазина
     */
    private ResponseEntity<StoreResponseDto> handleStoreCreationError(RuntimeException e) {
        log.error("❌ Business error creating store: {}", e.getMessage());

        if (e.getMessage().contains("already exists")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409
        } else if (e.getMessage().contains("geocode") || e.getMessage().contains("address")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 400
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500
        }
    }

    /**
     * 🏥 Проверка здоровья сервиса
     */
    @GetMapping("/brief/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "Store Service");
        return ResponseEntity.ok(health);
    }
}