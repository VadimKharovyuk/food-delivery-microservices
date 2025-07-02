package com.example.deliveryproductservice.service;
import com.example.deliveryproductservice.dto.FavoriteStore.FavoriteStoreApiResponse;
import com.example.deliveryproductservice.dto.FavoriteStore.FavoriteStoreResponseDto;

import java.util.List;

/**
 * Интерфейс сервиса для работы с избранными ресторанами
 */
public interface FavoriteStoreService {


    FavoriteStoreApiResponse<FavoriteStoreResponseDto> addToFavorites(Long userId, Long storeId);

    FavoriteStoreApiResponse<String> removeFromFavorites(Long userId, Long storeId);

    /**
     * Переключить статус избранного (добавить/удалить)
     * @param userId ID пользователя
     * @param storeId ID ресторана
     * @return результат операции
     */
    FavoriteStoreApiResponse<FavoriteStoreResponseDto> toggleFavorite(Long userId, Long storeId);


    FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>> getUserFavorites(Long userId);

    FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>> getUserActiveFavorites(Long userId);



    /**
     * Проверить, есть ли ресторан в избранном
     * @param userId ID пользователя
     * @param storeId ID ресторана
     * @return статус избранного
     */
    FavoriteStoreApiResponse<Boolean> isFavorite(Long userId, Long storeId);

    /**
     * Получить количество избранных ресторанов пользователя
     * @param userId ID пользователя
     * @return количество избранных ресторанов
     */
    FavoriteStoreApiResponse<Long> getUserFavoritesCount(Long userId);

    /**
     * Получить количество добавлений ресторана в избранное
     * @param storeId ID ресторана
     * @return количество добавлений
     */
    FavoriteStoreApiResponse<Long> getStoreFavoritesCount(Long storeId);
}