
package com.example.deliveryproductservice.service.impl;

import com.example.deliveryproductservice.dto.FavoriteStore.FavoriteStoreApiResponse;
import com.example.deliveryproductservice.dto.FavoriteStore.FavoriteStoreResponseDto;
import com.example.deliveryproductservice.mapper.FavoriteStoreMapper;
import com.example.deliveryproductservice.model.FavoriteStore;
import com.example.deliveryproductservice.model.Store;
import com.example.deliveryproductservice.repository.FavoriteStoreRepository;
import com.example.deliveryproductservice.repository.StoreRepository;
import com.example.deliveryproductservice.service.FavoriteStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FavoriteStoreServiceImpl implements FavoriteStoreService {

    private final FavoriteStoreRepository favoriteStoreRepository;
    private final StoreRepository storeRepository;
    private final FavoriteStoreMapper favoriteStoreMapper;


    @Override
    @Transactional
    public FavoriteStoreApiResponse<FavoriteStoreResponseDto> addToFavorites(Long userId, Long storeId) {
        log.info("🌟 Добавление ресторана {} в избранное пользователя {}", storeId, userId);

        try {
            // Валидация входных параметров
            if (userId == null || storeId == null) {
                return FavoriteStoreApiResponse.error("Некорректные параметры запроса");
            }

            // Проверяем, не добавлен ли уже ресторан в избранное
            if (favoriteStoreRepository.existsByUserIdAndStoreId(userId, storeId)) {
                log.warn("⚠️ Ресторан {} уже в избранном у пользователя {}", storeId, userId);
                return FavoriteStoreApiResponse.error("Ресторан уже в избранном");
            }

            // Проверяем существование ресторана
            Optional<Store> storeOpt = storeRepository.findById(storeId);
            if (storeOpt.isEmpty()) {
                log.warn("⚠️ Ресторан с ID {} не найден", storeId);
                return FavoriteStoreApiResponse.error("Ресторан не найден");
            }

            Store store = storeOpt.get();

            // Проверяем, активен ли ресторан
            if (!Boolean.TRUE.equals(store.getIsActive())) {
                log.warn("⚠️ Ресторан {} неактивен", storeId);
                return FavoriteStoreApiResponse.error("Нельзя добавить неактивный ресторан в избранное");
            }

            // Создаем запись избранного
            FavoriteStore favoriteStore = favoriteStoreMapper.createFavoriteStore(userId, store);
            FavoriteStore saved = favoriteStoreRepository.save(favoriteStore);

            // Конвертируем в DTO
            FavoriteStoreResponseDto responseDto = favoriteStoreMapper.toResponseDto(saved);

            log.info("✅ Ресторан {} успешно добавлен в избранное пользователя {}", storeId, userId);
            return FavoriteStoreApiResponse.success(responseDto, "Ресторан добавлен в избранное");

        } catch (Exception e) {
            log.error("💥 Ошибка добавления ресторана {} в избранное: {}", storeId, e.getMessage(), e);
            return FavoriteStoreApiResponse.error("Ошибка добавления в избранное");
        }
    }

    @Override
    @Transactional
    public FavoriteStoreApiResponse<String> removeFromFavorites(Long userId, Long storeId) {
        log.info("🗑️ Удаление ресторана {} из избранного пользователя {}", storeId, userId);

        try {
            // Валидация входных параметров
            if (userId == null || storeId == null) {
                return FavoriteStoreApiResponse.error("Некорректные параметры запроса");
            }

            // Проверяем существование записи
            Optional<FavoriteStore> favoriteOpt = favoriteStoreRepository.findByUserIdAndStoreId(userId, storeId);
            if (favoriteOpt.isEmpty()) {
                log.warn("⚠️ Ресторан {} не найден в избранном у пользователя {}", storeId, userId);
                return FavoriteStoreApiResponse.error("Ресторан не найден в избранном");
            }

            // Удаляем запись
            favoriteStoreRepository.delete(favoriteOpt.get());

            log.info("✅ Ресторан {} удален из избранного пользователя {}", storeId, userId);
            return FavoriteStoreApiResponse.success("removed", "Ресторан удален из избранного");

        } catch (Exception e) {
            log.error("💥 Ошибка удаления ресторана {} из избранного: {}", storeId, e.getMessage(), e);
            return FavoriteStoreApiResponse.error("Ошибка удаления из избранного");
        }
    }

    @Override
    @Transactional
    public FavoriteStoreApiResponse<FavoriteStoreResponseDto> toggleFavorite(Long userId, Long storeId) {
        log.info("🔄 Переключение избранного для ресторана {} пользователя {}", storeId, userId);

        boolean isFavorite = favoriteStoreRepository.existsByUserIdAndStoreId(userId, storeId);

        if (isFavorite) {
            // Удаляем из избранного
            FavoriteStoreApiResponse<String> removeResult = removeFromFavorites(userId, storeId);
            if (removeResult.getSuccess()) {
                return FavoriteStoreApiResponse.success(null, "Ресторан удален из избранного");
            } else {
                return FavoriteStoreApiResponse.error(removeResult.getMessage());
            }
        } else {
            // Добавляем в избранное
            return addToFavorites(userId, storeId);
        }
    }



    @Override
    public FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>> getUserFavorites(Long userId) {
        log.info("📋 Получение избранных ресторанов пользователя {}", userId);

        try {
            if (userId == null) {
                return FavoriteStoreApiResponse.error("ID пользователя не указан");
            }

            List<FavoriteStore> favorites = favoriteStoreRepository.findByUserIdWithStore(userId);
            List<FavoriteStoreResponseDto> responseList = favoriteStoreMapper.toResponseDtoList(favorites);

            log.info("✅ Найдено {} избранных ресторанов для пользователя {}", responseList.size(), userId);
            return FavoriteStoreApiResponse.success(responseList,
                    String.format("Найдено %d избранных ресторанов", responseList.size()));

        } catch (Exception e) {
            log.error("💥 Ошибка получения избранных ресторанов пользователя {}: {}", userId, e.getMessage(), e);
            return FavoriteStoreApiResponse.error("Ошибка получения избранных ресторанов");
        }
    }

    @Override
    public FavoriteStoreApiResponse<List<FavoriteStoreResponseDto>> getUserActiveFavorites(Long userId) {
        log.info("📋 Получение активных избранных ресторанов пользователя {}", userId);

        try {
            if (userId == null) {
                return FavoriteStoreApiResponse.error("ID пользователя не указан");
            }

            List<FavoriteStore> activeFavorites = favoriteStoreRepository.findActiveByUserId(userId);
            List<FavoriteStoreResponseDto> responseList = favoriteStoreMapper.toResponseDtoList(activeFavorites);

            log.info("✅ Найдено {} активных избранных ресторанов для пользователя {}", responseList.size(), userId);
            return FavoriteStoreApiResponse.success(responseList,
                    String.format("Найдено %d активных избранных ресторанов", responseList.size()));

        } catch (Exception e) {
            log.error("💥 Ошибка получения активных избранных ресторанов пользователя {}: {}", userId, e.getMessage(), e);
            return FavoriteStoreApiResponse.error("Ошибка получения активных избранных ресторанов");
        }
    }

    // ================================
    // ПРОВЕРКИ И СТАТИСТИКА
    // ================================

    @Override
    public FavoriteStoreApiResponse<Boolean> isFavorite(Long userId, Long storeId) {
        log.info("🔍 Проверка избранного ресторана {} для пользователя {}", storeId, userId);

        try {
            if (userId == null || storeId == null) {
                return FavoriteStoreApiResponse.error("Некорректные параметры запроса");
            }

            boolean isFavorite = favoriteStoreRepository.existsByUserIdAndStoreId(userId, storeId);

            log.info("✅ Ресторан {} {} в избранном у пользователя {}",
                    storeId, isFavorite ? "находится" : "не находится", userId);

            return FavoriteStoreApiResponse.success(isFavorite,
                    isFavorite ? "Ресторан в избранном" : "Ресторан не в избранном");

        } catch (Exception e) {
            log.error("💥 Ошибка проверки избранного: {}", e.getMessage(), e);
            return FavoriteStoreApiResponse.error("Ошибка проверки избранного");
        }
    }

    @Override
    public FavoriteStoreApiResponse<Long> getUserFavoritesCount(Long userId) {
        log.info("📊 Получение количества избранных ресторанов пользователя {}", userId);

        try {
            if (userId == null) {
                return FavoriteStoreApiResponse.error("ID пользователя не указан");
            }

            long count = favoriteStoreRepository.countByUserId(userId);

            log.info("✅ У пользователя {} найдено {} избранных ресторанов", userId, count);
            return FavoriteStoreApiResponse.success(count,
                    String.format("У пользователя %d избранных ресторанов", count));

        } catch (Exception e) {
            log.error("💥 Ошибка подсчета избранных ресторанов пользователя {}: {}", userId, e.getMessage(), e);
            return FavoriteStoreApiResponse.error("Ошибка подсчета избранных ресторанов");
        }
    }

    @Override
    public FavoriteStoreApiResponse<Long> getStoreFavoritesCount(Long storeId) {
        log.info("📊 Получение количества добавлений ресторана {} в избранное", storeId);

        try {
            if (storeId == null) {
                return FavoriteStoreApiResponse.error("ID ресторана не указан");
            }

            long count = favoriteStoreRepository.countByStoreId(storeId);

            log.info("✅ Ресторан {} добавлен в избранное {} раз", storeId, count);
            return FavoriteStoreApiResponse.success(count,
                    String.format("Ресторан добавлен в избранное %d раз", count));

        } catch (Exception e) {
            log.error("💥 Ошибка подсчета добавлений ресторана {} в избранное: {}", storeId, e.getMessage(), e);
            return FavoriteStoreApiResponse.error("Ошибка подсчета добавлений в избранное");
        }
    }
}