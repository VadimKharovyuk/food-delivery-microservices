// StoreController.java
package com.example.deliveryproductservice.controller;

import com.example.deliveryproductservice.annotation.CurrentUser;
import com.example.deliveryproductservice.dto.StoreDto.CreateStoreDto;
import com.example.deliveryproductservice.dto.StoreDto.SingleStoreResponseWrapper;
import com.example.deliveryproductservice.dto.StoreDto.StoreResponseDto;
import com.example.deliveryproductservice.dto.StoreDto.StoreResponseWrapper;
import com.example.deliveryproductservice.service.StoreService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Получить магазины текущего пользователя (владельца)
     * GET /api/stores/my
     */
    @GetMapping("/my")
    public ResponseEntity<StoreResponseWrapper> getMyStores(
            @CurrentUser Long ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("📋 GET /api/stores/my - Getting stores for current user: page={}, size={}", page, size);

        StoreResponseWrapper response = storeService.getStoresByOwner(ownerId, page, size);

        log.info("✅ Found {} stores for user {}, hasNext: {}", response.getTotalCount(), ownerId, response.getHasNext());
        return ResponseEntity.ok(response);
    }

    /**
     * Получить магазины конкретного владельца
     * GET /api/stores/owner/{ownerId}
     */
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<StoreResponseWrapper> getStoresByOwner(
            @PathVariable Long ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("📋 GET /api/stores/owner/{} - Getting stores for owner: page={}, size={}", ownerId, page, size);

        StoreResponseWrapper response = storeService.getStoresByOwner(ownerId, page, size);

        log.info("✅ Found {} stores for owner {}, hasNext: {}", response.getTotalCount(), ownerId, response.getHasNext());
        return ResponseEntity.ok(response);
    }

    /**
     * Получить магазин по ID
     * GET /api/stores/{id}
     */
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


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoreResponseDto> createStore(
            @Valid @ModelAttribute CreateStoreDto createStoreDto,
            @CurrentUser Long userId,
            HttpServletRequest request) {

        log.info("🏪 POST /api/stores - Creating new store: {}", createStoreDto.getName());

        // 🔐 Проверяем авторизацию и роль
        String userEmail = request.getHeader("X-User-Email");
        String userRole = request.getHeader("X-User-Role");

        log.info("🔐 Store creation request from User: ID={}, Email={}, Role={}",
                userId, userEmail, userRole);

        // Проверяем роль BUSINESS_USER
        if (!"ROLE_BUSINESS".equals(userRole)) {
            log.warn("❌ Access denied for user {} with role {}. Required: ROLE_BUSINESS",
                    userId, userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .build();
        }

        try {
            // 🏗️ Создаем магазин
            StoreResponseDto createdStore = storeService.createStore(createStoreDto, userId);

            log.info("✅ Store created successfully: {} (ID: {}) by user {}",
                    createdStore.getName(), createdStore.getId(), userId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createdStore);

        } catch (RuntimeException e) {
            log.error("❌ Business error creating store: {}", e.getMessage());

            // Определяем тип ошибки для правильного HTTP статуса
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .build(); // 409 Conflict
            } else if (e.getMessage().contains("geocode") || e.getMessage().contains("address")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .build(); // 400 Bad Request
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build(); // 500 Internal Server Error
            }

        } catch (Exception e) {
            log.error("❌ Unexpected error creating store: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    // ================================
    // 🔧 ДОПОЛНИТЕЛЬНЫЕ ENDPOINTS (для развития)
    // ================================

    /**
     * 🧪 Тестовый endpoint для проверки доступности API
     * GET /api/stores/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.debug("🔧 GET /api/stores/health - Health check");
        return ResponseEntity.ok("Stores API is up and running! 🏪");
    }




    /**
     * 🏪 Создать новый магазин (JSON версия без изображения)
     * POST /api/stores/simple
     *
     * Доступ: только ROLE_BUSINESS
     * Принимает JSON данные без изображения
     */
    @PostMapping("/simple")
    public ResponseEntity<StoreResponseDto> createStoreSimple(
            @Valid @RequestBody CreateStoreDto createStoreDto,
            @CurrentUser Long userId,
            HttpServletRequest request) {

        log.info("🏪 POST /api/stores/simple - Creating new store: {}", createStoreDto.getName());

        // 🔐 Проверяем авторизацию и роль
        String userEmail = request.getHeader("X-User-Email");
        String userRole = request.getHeader("X-User-Role");

        log.info("🔐 Store creation request from User: ID={}, Email={}, Role={}",
                userId, userEmail, userRole);

        // Проверяем роль BUSINESS_USER
        if (!"ROLE_BUSINESS".equals(userRole)) {
            log.warn("❌ Access denied for user {} with role {}. Required: ROLE_BUSINESS",
                    userId, userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .build();
        }

        try {
            // 🏗️ Создаем магазин
            StoreResponseDto createdStore = storeService.createStore(createStoreDto, userId);

            log.info("✅ Store created successfully: {} (ID: {}) by user {}",
                    createdStore.getName(), createdStore.getId(), userId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createdStore);

        } catch (RuntimeException e) {
            log.error("❌ Business error creating store: {}", e.getMessage());

            // Определяем тип ошибки для правильного HTTP статуса
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .build(); // 409 Conflict
            } else if (e.getMessage().contains("geocode") || e.getMessage().contains("address")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .build(); // 400 Bad Request
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build(); // 500 Internal Server Error
            }

        } catch (Exception e) {
            log.error("❌ Unexpected error creating store: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }


}




