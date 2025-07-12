package com.example.deliveryproductservice.controller;

import com.example.deliveryproductservice.annotation.CurrentUser;
import com.example.deliveryproductservice.dto.FavoriteStore.FavoriteStoreApiResponse;
import com.example.deliveryproductservice.dto.FavoriteStore.FavoriteStoreResponseDto;
import com.example.deliveryproductservice.service.FavoriteStoreService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/favorites")
@Slf4j
public class FavoriteStoreController {

    private final FavoriteStoreService favoriteStoreService;

    /**
     * Получить все избранные рестораны текущего пользователя
     * GET /api/favorites
     */
    @GetMapping
    public ResponseEntity<FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>>> getMyFavorites(
            @CurrentUser Long userId) {

        log.info("📋 REST: Получение избранных ресторанов пользователя {} (из JWT)", userId);

        // Проверка авторизации
        if (userId == null) {
            log.warn("❌ Пользователь не авторизован");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(FavoriteStoreApiResponse.error("Требуется авторизация"));
        }

        FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>> response =
                favoriteStoreService.getUserFavorites(userId);

        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/stores/{storeId}")
    public ResponseEntity<FavoriteStoreApiResponse<FavoriteStoreResponseDto>> addToFavorites(
            @CurrentUser Long userId,
            @PathVariable Long storeId) {

        log.info("🌟 REST: Добавление ресторана {} в избранное пользователя {} (из JWT)", storeId, userId);

        // Проверка авторизации
        if (userId == null) {
            log.warn("❌ Пользователь не авторизован");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(FavoriteStoreApiResponse.error("Требуется авторизация"));
        }

        FavoriteStoreApiResponse<FavoriteStoreResponseDto> response =
                favoriteStoreService.addToFavorites(userId, storeId);

        HttpStatus status = response.getSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Удалить ресторан из избранного для текущего пользователя
     * DELETE /api/favorites/stores/{storeId}
     */
    @DeleteMapping("/stores/{storeId}")
    public ResponseEntity<FavoriteStoreApiResponse<String>> removeFromFavorites(
            @CurrentUser Long userId,
            @PathVariable Long storeId) {

        log.info("🗑️ REST: Удаление ресторана {} из избранного пользователя {} (из JWT)", storeId, userId);

        // Проверка авторизации
        if (userId == null) {
            log.warn("❌ Пользователь не авторизован");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(FavoriteStoreApiResponse.error("Требуется авторизация"));
        }

        FavoriteStoreApiResponse<String> response =
                favoriteStoreService.removeFromFavorites(userId, storeId);

        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Переключить статус избранного для текущего пользователя
     * PUT /api/favorites/stores/{storeId}/toggle
     */
    @PutMapping("/stores/{storeId}/toggle")
    public ResponseEntity<FavoriteStoreApiResponse<FavoriteStoreResponseDto>> toggleFavorite(
            @CurrentUser Long userId,
            @PathVariable Long storeId) {

        log.info("🔄 REST: Переключение избранного ресторана {} для пользователя {} (из JWT)", storeId, userId);

        // Проверка авторизации
        if (userId == null) {
            log.warn("❌ Пользователь не авторизован");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(FavoriteStoreApiResponse.error("Требуется авторизация"));
        }

        FavoriteStoreApiResponse<FavoriteStoreResponseDto> response =
                favoriteStoreService.toggleFavorite(userId, storeId);

        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }




    /**
     * Получить только активные избранные рестораны текущего пользователя
     * GET /api/favorites/active
     */
    @GetMapping("/active")
    public ResponseEntity<FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>>> getMyActiveFavorites(
            @CurrentUser Long userId) {

        log.info("📋 REST: Получение активных избранных ресторанов пользователя {} (из JWT)", userId);

        // Проверка авторизации
        if (userId == null) {
            log.warn("❌ Пользователь не авторизован");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(FavoriteStoreApiResponse.error("Требуется авторизация"));
        }

        FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>> response =
                favoriteStoreService.getUserActiveFavorites(userId);

        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Проверить, есть ли ресторан в избранном у текущего пользователя
     * GET /api/favorites/stores/{storeId}/status
     */
    @GetMapping("/stores/{storeId}/status")
    public ResponseEntity<FavoriteStoreApiResponse<Boolean>> checkMyFavoriteStatus(
            @CurrentUser Long userId,
            @PathVariable Long storeId) {

        log.info("🔍 REST: Проверка статуса избранного ресторана {} для пользователя {} (из JWT)", storeId, userId);

        // Проверка авторизации
        if (userId == null) {
            log.warn("❌ Пользователь не авторизован");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(FavoriteStoreApiResponse.error("Требуется авторизация"));
        }

        FavoriteStoreApiResponse<Boolean> response =
                favoriteStoreService.isFavorite(userId, storeId);

        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Получить количество избранных ресторанов текущего пользователя
     * GET /api/favorites/count
     */
    @GetMapping("/count")
    public ResponseEntity<FavoriteStoreApiResponse<Long>> getMyFavoritesCount(
            @CurrentUser Long userId) {

        log.info("📊 REST: Получение количества избранных ресторанов пользователя {} (из JWT)", userId);

        // Проверка авторизации
        if (userId == null) {
            log.warn("❌ Пользователь не авторизован");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(FavoriteStoreApiResponse.error("Требуется авторизация"));
        }

        FavoriteStoreApiResponse<Long> response =
                favoriteStoreService.getUserFavoritesCount(userId);

        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Добавить в избранное через JSON body
     * POST /api/favorites
     */
    @PostMapping
    public ResponseEntity<FavoriteStoreApiResponse<FavoriteStoreResponseDto>> addToFavoritesWithBody(
            @CurrentUser Long userId,
            @RequestBody AddToFavoriteRequest request) {

        log.info("🌟 REST: Добавление ресторана {} в избранное пользователя {} (из JWT, через body)",
                request.getStoreId(), userId);

        // Проверка авторизации
        if (userId == null) {
            log.warn("❌ Пользователь не авторизован");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(FavoriteStoreApiResponse.error("Требуется авторизация"));
        }

        FavoriteStoreApiResponse<FavoriteStoreResponseDto> response =
                favoriteStoreService.addToFavorites(userId, request.getStoreId());

        HttpStatus status = response.getSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * DTO для запроса добавления в избранное
     */
    @Setter
    @Getter
    public static class AddToFavoriteRequest {
        private Long storeId;

        public AddToFavoriteRequest() {}
        public AddToFavoriteRequest(Long storeId) { this.storeId = storeId; }

    }
}