package com.example.deliveryproductservice.controller;
import com.example.deliveryproductservice.annotation.CurrentUser;
import com.example.deliveryproductservice.dto.StoreDto.*;
import com.example.deliveryproductservice.dto.category.ApiResponse;
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
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@Slf4j
public class StoreController {

    private final StoreService storeService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<StoreResponseDto>> createStore(
            @RequestPart("store") CreateStoreDto createStoreRequest,
            @RequestHeader("X-User-Id") Long userId,
            @RequestPart("imageFile") MultipartFile imageFile) {

        log.info("📸 Creating store with image: {}", createStoreRequest.getName());
        log.info("📋 Image file: {} ({} bytes)",
                imageFile.getOriginalFilename(),
                imageFile.getSize());

        try {
            // Устанавливаем файл в объект запроса
            createStoreRequest.setImageFile(imageFile);

            // Ваша логика создания магазина...
            StoreResponseDto storeResponse = storeService.createStore(createStoreRequest,userId);

            return ResponseEntity.ok(ApiResponse.success(storeResponse));

        } catch (Exception e) {
            log.error("💥 Error creating store", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Ошибка создания магазина: " + e.getMessage()));
        }
    }



    /**
     * ✅ Валидация основных полей запроса
     */
    private void validateStoreRequest(CreateStoreDto createStoreDto) {
        if (createStoreDto.getName() == null || createStoreDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Название магазина обязательно");
        }

        if (createStoreDto.getStreet() == null || createStoreDto.getStreet().trim().isEmpty()) {
            throw new IllegalArgumentException("Адрес (улица) обязателен");
        }

        if (createStoreDto.getCity() == null || createStoreDto.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("Город обязателен");
        }

        if (createStoreDto.getCountry() == null || createStoreDto.getCountry().trim().isEmpty()) {
            throw new IllegalArgumentException("Страна обязательна");
        }

        // Проверка изображения (если требуется)
        MultipartFile imageFile = createStoreDto.getImageFile();
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Изображение магазина обязательно");
        }

        // Проверка размера файла (5MB)
        if (imageFile.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Размер изображения не должен превышать 5MB");
        }

        // Проверка типа файла
        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Файл должен быть изображением");
        }

        log.debug("✅ Store request validation passed for: {}", createStoreDto.getName());
    }




    /**
     * ✅ Детальное логирование входящего запроса
     */
    private void logIncomingRequest(HttpServletRequest request, CreateStoreDto createStoreDto) {
        log.info("🔍 === INCOMING REQUEST DEBUG ===");
        log.info("🌐 Method: {}", request.getMethod());
        log.info("🌐 URL: {}", request.getRequestURL());
        log.info("🌐 Content-Type: {}", request.getContentType());
        log.info("🌐 Content-Length: {}", request.getContentLength());

        // Логируем заголовки
        log.info("📋 Headers:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            if ("authorization".equalsIgnoreCase(headerName)) {
                log.info("   🔐 {}: Bearer ***", headerName);
            } else {
                log.info("   📋 {}: {}", headerName, headerValue);
            }
        }

        if (createStoreDto != null) {
            log.info("🏪 Store Data: name={}, city={}", createStoreDto.getName(), createStoreDto.getCity());
            if (createStoreDto.getImageFile() != null) {
                log.info("🖼️ Image: name={}, size={}KB, type={}",
                        createStoreDto.getImageFile().getOriginalFilename(),
                        createStoreDto.getImageFile().getSize() / 1024,
                        createStoreDto.getImageFile().getContentType());
            } else {
                log.info("🖼️ Image: none");
            }
        }

        log.info("🔍 === END REQUEST DEBUG ===");
    }

    /**
     * ✅ Обработчик multipart ошибок
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<String> handleMultipartException(MultipartException e) {
        log.error("❌ Multipart exception: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body("Multipart error: " + e.getMessage());
    }

    /**
     * ✅ Простой DTO для тестирования
     */
    @lombok.Data
    public static class TestDto {
        private String data;
    }


    @GetMapping("/ui")
    public ResponseEntity<StoreUIResponseWrapper> getStoresForUI() {
        log.debug("Getting stores for UI");
        StoreUIResponseWrapper response = storeService.getActiveStoresForUI();

        log.debug("Response contains {} stores", response.getStores().size());
        response.getStores().forEach(store ->
                log.debug("Store: {}, picUrl: {}", store.getName(), store.getPicUrl())
        );
        return response.getSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }


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