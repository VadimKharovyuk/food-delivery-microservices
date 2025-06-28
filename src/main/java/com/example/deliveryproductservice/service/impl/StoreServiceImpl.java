// StoreServiceImpl.java - Полная реализация создания магазина
package com.example.deliveryproductservice.service.impl;

import com.example.deliveryproductservice.dto.StoreDto.CreateStoreDto;
import com.example.deliveryproductservice.dto.StoreDto.StoreResponseDto;
import com.example.deliveryproductservice.mapper.StoreMapper;
import com.example.deliveryproductservice.model.Address;
import com.example.deliveryproductservice.model.Store;
import com.example.deliveryproductservice.repository.StoreRepository;
import com.example.deliveryproductservice.service.GeocodingService;
import com.example.deliveryproductservice.service.StorageService;
import com.example.deliveryproductservice.service.StoreService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;
    private final StorageService storageService;
    private final GeocodingService geocodingService;

    @Override
    public StoreResponseDto createStore(CreateStoreDto createStoreDto, Long ownerId) {
        log.info("Creating new store: {} by user: {}", createStoreDto.getName(), ownerId);

        try {
            // 1. 🌍 Создаем адрес с координатами через геокодирование
            Address storeAddress = createStoreAddress(createStoreDto);
            log.info("📍 Address created with coordinates: [{}, {}]",
                    storeAddress.getLatitude(), storeAddress.getLongitude());

            // 2. 📸 Загружаем изображение магазина (если есть)
            ImageUploadResult imageResult = handleImageUpload(createStoreDto.getImageFile(), "stores");

            // 3. 🏪 Создаем сущность Store
            Store store = buildStoreEntity(createStoreDto, ownerId, storeAddress, imageResult);

            // 4. 💾 Сохраняем в базу данных
            Store savedStore = storeRepository.save(store);

            log.info("✅ Store created successfully with ID: {} by owner: {}",
                    savedStore.getId(), ownerId);

            // 5. 📊 Логируем статистику создания
            logStoreCreationStats(savedStore);

            // 6. 📤 Возвращаем DTO ответ
            return storeMapper.mapToResponseDto(savedStore);

        } catch (Exception e) {
            log.error("❌ Error creating store for owner {}: {}", ownerId, e.getMessage(), e);
            throw new RuntimeException("Failed to create store: " + e.getMessage(), e);
        }
    }

    // ================================
    // 🛠️ ПРИВАТНЫЕ МЕТОДЫ
    // ================================

    /**
     * 🌍 Создание адреса с геокодированием
     */
    private Address createStoreAddress(CreateStoreDto createStoreDto) {
        try {
            log.debug("🔍 Creating address with geocoding for: {}",
                    createStoreDto.getAddress().getStreet());

            Address address = geocodingService.createAddressWithCoordinates(createStoreDto.getAddress());

            log.info("📍 Geocoding successful: {} → [{}, {}]",
                    address.getFormattedAddress(),
                    address.getLatitude(),
                    address.getLongitude());

            return address;

        } catch (Exception e) {
            log.error("❌ Geocoding failed for address: {}",
                    createStoreDto.getAddress().getStreet(), e);
            throw new RuntimeException("Failed to geocode store address: " + e.getMessage(), e);
        }
    }

    /**
     * 📸 Загрузка изображения магазина
     */
    private ImageUploadResult handleImageUpload(MultipartFile imageFile, String folder) {
        if (imageFile == null || imageFile.isEmpty()) {
            log.debug("No image file provided for store");
            return new ImageUploadResult(null, null);
        }

        try {
            log.info("📸 Uploading store image: {}", imageFile.getOriginalFilename());

            StorageService.StorageResult storageResult = storageService.uploadImage(imageFile, folder);

            log.info("✅ Store image uploaded successfully: {} with ID: {}",
                    storageResult.getUrl(), storageResult.getImageId());

            return new ImageUploadResult(storageResult.getUrl(), storageResult.getImageId());

        } catch (IOException e) {
            log.error("❌ Failed to upload store image: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload store image: " + e.getMessage(), e);
        }
    }

    /**
     * 🏪 Создание сущности Store
     */
    private Store buildStoreEntity(CreateStoreDto dto, Long ownerId, Address address, ImageUploadResult imageResult) {
        log.debug("🔨 Building Store entity for owner: {}", ownerId);

        Store store = new Store();

        // Основная информация
        store.setOwnerId(ownerId);
        store.setName(dto.getName());
        store.setDescription(dto.getDescription());
        store.setAddress(address);

        // Контактная информация
        store.setPhone(dto.getPhone());
        store.setEmail(dto.getEmail());

        // Настройки доставки
        store.setDeliveryRadius(dto.getDeliveryRadius());
        store.setDeliveryFee(dto.getDeliveryFee());
        store.setEstimatedDeliveryTime(dto.getEstimatedDeliveryTime());

        // Изображение
        if (imageResult.getImageUrl() != null) {
            store.setPicUrl(imageResult.getImageUrl());
            store.setPicId(imageResult.getImageId());
        } else {
            // Дефолтное изображение для магазинов
            store.setPicUrl(getDefaultStoreImageUrl());
            store.setPicId("default_store_image");
        }

        // Статус и рейтинг
        store.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        // rating остается BigDecimal.ZERO по умолчанию

        log.debug("✅ Store entity built successfully");
        return store;
    }

    /**
     * 🖼️ Получение URL дефолтного изображения
     */
    private String getDefaultStoreImageUrl() {
        // Можно настроить через properties или использовать Cloudinary
        return "https://via.placeholder.com/800x600/f0f0f0/999999?text=Store+Image";
    }

    /**
     * 🔍 Валидация уникальности названия магазина
     */
    private void validateStoreUniqueness(String storeName, Long ownerId) {
        boolean exists = storeRepository.existsByNameAndOwnerIdAndIsActiveTrue(storeName, ownerId);
        if (exists) {
            throw new RuntimeException("Store with name '" + storeName + "' already exists for this owner");
        }
    }

    /**
     * 📊 Логирование статистики создания
     */
    private void logStoreCreationStats(Store savedStore) {
        log.info("📊 Store creation stats:");
        log.info("   🏪 Store ID: {}", savedStore.getId());
        log.info("   👤 Owner ID: {}", savedStore.getOwnerId());
        log.info("   📍 Location: {}", savedStore.getAddress().getFormattedAddress());
        log.info("   🚚 Delivery: {}km radius, ${} fee, {} min",
                savedStore.getDeliveryRadius(),
                savedStore.getDeliveryFee(),
                savedStore.getEstimatedDeliveryTime());
        log.info("   🖼️ Image: {}", savedStore.getPicUrl() != null ? "Yes" : "No");
    }

    // ================================
    // 📦 ВСПОМОГАТЕЛЬНЫЕ КЛАССЫ
    // ================================

    /**
     * Результат загрузки изображения
     */
    @Getter
    private static class ImageUploadResult {
        private final String imageUrl;
        private final String imageId;

        public ImageUploadResult(String imageUrl, String imageId) {
            this.imageUrl = imageUrl;
            this.imageId = imageId;
        }

    }
}