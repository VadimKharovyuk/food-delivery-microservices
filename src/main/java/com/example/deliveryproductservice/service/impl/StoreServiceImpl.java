// StoreServiceImpl.java - Полная реализация создания магазина
package com.example.deliveryproductservice.service.impl;

import com.example.deliveryproductservice.dto.StoreDto.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;
    private final StorageService storageService;
    private final GeocodingService geocodingService;
    private static final int UI_STORE_LIMIT = 6;

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

    @Override
    @Transactional(readOnly = true)
    public StoreResponseWrapper getActiveStores(int page, int size) {
        log.debug("Getting active stores with pagination: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Slice<Store> storeSlice = storeRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);

        Slice<StoreResponseDto> storeDtoSlice = storeSlice.map(storeMapper::mapToResponseDto);

        return StoreResponseWrapper.success(storeDtoSlice);
    }

    // Для UI с лимитом (возвращает только нужные поля)
    @Transactional(readOnly = true)
    public StoreUIResponseWrapper getActiveStoresForUI() {
        log.debug("Getting limited active stores for UI with limit: {}", UI_STORE_LIMIT);

        try {
            Pageable pageable = PageRequest.of(0, UI_STORE_LIMIT);

            // Используем проекцию - достаем только нужные поля
            Slice<StoreUIProjection> storeProjections =
                    storeRepository.findByIsActiveTrueOrderByRatingDescCreatedAtDesc(pageable);

            List<StoreUIDto> uiStores = storeProjections.getContent().stream()
                    .map(storeMapper::mapProjectionToUIDto)
                    .collect(Collectors.toList());

            return StoreUIResponseWrapper.success(uiStores);

        } catch (Exception e) {
            log.error("Error getting stores for UI", e);
            return StoreUIResponseWrapper.error("Ошибка получения списка магазинов");
        }
    }



    @Transactional(readOnly = true)
    @Override
    public StoreResponseWrapper getStoresByOwner(Long ownerId, int page, int size) {
        log.debug("Getting stores for owner {} with pagination: page={}, size={}", ownerId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Slice<Store> storeSlice = storeRepository.findByOwnerIdAndIsActiveTrueOrderByCreatedAtDesc(ownerId, pageable);

        Slice<StoreResponseDto> storeDtoSlice = storeSlice.map(storeMapper::mapToResponseDto);

        return StoreResponseWrapper.success(storeDtoSlice);
    }
@Override
    @Transactional(readOnly = true)
    public SingleStoreResponseWrapper getStoreById(Long storeId) {
        log.debug("Getting store by ID: {}", storeId);

        Optional<Store> storeOptional = storeRepository.findByIdAndIsActiveTrue(storeId);

        if (storeOptional.isPresent()) {
            StoreResponseDto storeDto = storeMapper.mapToResponseDto(storeOptional.get());
            return SingleStoreResponseWrapper.success(storeDto);
        } else {
            return SingleStoreResponseWrapper.notFound(storeId);
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
    private Store buildStoreEntity(CreateStoreDto createStoreDto, Long ownerId, Address storeAddress, ImageUploadResult imageResult) {
        Store store = new Store();
        store.setOwnerId(ownerId);
        store.setName(createStoreDto.getName());
        store.setDescription(createStoreDto.getDescription());
        store.setAddress(storeAddress);
        store.setPhone(createStoreDto.getPhone());
        store.setEmail(createStoreDto.getEmail());
        store.setIsActive(createStoreDto.getIsActive());
        store.setDeliveryRadius(createStoreDto.getDeliveryRadius());
        store.setDeliveryFee(createStoreDto.getDeliveryFee());
        store.setEstimatedDeliveryTime(createStoreDto.getEstimatedDeliveryTime());

        // Генерируем уникальный pic_id
        if (imageResult != null && imageResult.getImageId() != null) {
            store.setPicId(imageResult.getImageId());
            store.setPicUrl(imageResult.getImageUrl());
        } else {
            // Генерируем уникальный ID для дефолтного изображения
            String uniquePicId = "default_store_" + System.currentTimeMillis() + "_" + ownerId;
            store.setPicId(uniquePicId);
            store.setPicUrl("https://via.placeholder.com/800x600/f0f0f0/999999?text=Store+Image");
        }

        store.setRating(BigDecimal.ZERO);

        return store;
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