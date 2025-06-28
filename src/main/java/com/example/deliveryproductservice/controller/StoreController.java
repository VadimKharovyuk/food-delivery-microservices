// StoreController.java
package com.example.deliveryproductservice.controller;

import com.example.deliveryproductservice.annotation.CurrentUser;
import com.example.deliveryproductservice.dto.StoreDto.CreateStoreDto;
import com.example.deliveryproductservice.dto.StoreDto.StoreResponseDto;
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

    /**
     * 🏪 Создать новый магазин
     * POST /api/stores
     *
     * Доступ: только ROLE_BUSINESS
     * Поддерживает загрузку изображения через multipart/form-data
     */
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
     * ℹ️ Информация о сервисе магазинов
     * GET /api/stores/info
     */
    @GetMapping("/info")
    public ResponseEntity<StoreServiceInfo> getServiceInfo() {
        log.debug("ℹ️ GET /api/stores/info - Getting service info");

        StoreServiceInfo info = new StoreServiceInfo(
                "Stores Management Service",
                "1.0.0",
                "Manages store creation and operations for BUSINESS users",
                "Active"
        );

        return ResponseEntity.ok(info);
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

    // ================================
    // 📦 ВСПОМОГАТЕЛЬНЫЕ КЛАССЫ
    // ================================

    /**
     * Информация о сервисе магазинов
     */
    @Getter
    public static class StoreServiceInfo {
        // Getters
        private final String serviceName;
        private final String version;
        private final String description;
        private final String status;

        public StoreServiceInfo(String serviceName, String version, String description, String status) {
            this.serviceName = serviceName;
            this.version = version;
            this.description = description;
            this.status = status;
        }

    }
}




