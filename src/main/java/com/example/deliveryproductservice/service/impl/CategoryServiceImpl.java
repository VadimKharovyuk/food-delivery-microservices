package com.example.deliveryproductservice.service.impl;
import com.example.deliveryproductservice.dto.category.*;
import com.example.deliveryproductservice.mapper.CategoryMapper;
import com.example.deliveryproductservice.model.Category;
import com.example.deliveryproductservice.repository.CategoryRepository;
import com.example.deliveryproductservice.service.CategoryService;
import com.example.deliveryproductservice.service.StorageService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final StorageService storageService;

    // ================================
    // ✏️ CRUD ОПЕРАЦИИ
    // ================================

    @Override
    public ApiResponse<CategoryResponseDto> createCategory(CreateCategoryDto dto, Long createdBy) {
        log.info("Creating new category: {} by user: {}", dto.getName(), createdBy);

        try {
            // Проверяем уникальность имени
            if (existsActiveCategoryByName(dto.getName())) {
                return ApiResponse.error("Категория с названием '" + dto.getName() + "' уже существует");
            }

            ImageUploadResult imageResult = handleImageUpload(dto.getImageFile(), "categories");
            Category category = categoryMapper.mapToEntity(dto, imageResult.getImageUrl());

            if (imageResult.getImageId() != null) {
                category.setImageId(imageResult.getImageId());
            }

            Category savedCategory = categoryRepository.save(category);
            CategoryResponseDto responseDto = categoryMapper.mapToResponseDto(savedCategory);

            log.info("✅ Category created: {}", savedCategory.getId());
            return ApiResponse.success(responseDto);

        } catch (Exception e) {
            log.error("Error creating category: {}", dto.getName(), e);
            return ApiResponse.error("Ошибка создания категории");
        }
    }

    @Override
    public ApiResponse<CategoryResponseDto> updateCategory(Long id, CreateCategoryDto dto, Long updatedBy) {
        log.info("Updating category: {} by user: {}", id, updatedBy);

        try {
            Optional<Category> categoryOpt = categoryRepository.findById(id);
            if (categoryOpt.isEmpty()) {
                return ApiResponse.notFound("Категория с ID " + id + " не найдена");
            }

            Category category = categoryOpt.get();

            // Проверяем уникальность имени (если имя изменилось)
            if (!category.getName().equals(dto.getName()) && existsActiveCategoryByName(dto.getName())) {
                return ApiResponse.error("Категория с названием '" + dto.getName() + "' уже существует");
            }

            ImageUploadResult imageResult = handleImageUpdate(
                    dto.getImageFile(),
                    category.getImageId(),
                    "categories"
            );

            categoryMapper.updateEntityFromDto(category, dto, imageResult.getImageUrl());

            if (imageResult.getImageId() != null) {
                category.setImageId(imageResult.getImageId());
            }

            Category savedCategory = categoryRepository.save(category);
            CategoryResponseDto responseDto = categoryMapper.mapToResponseDto(savedCategory);

            log.info("✅ Category updated: {}", savedCategory.getId());
            return ApiResponse.success(responseDto);

        } catch (Exception e) {
            log.error("Error updating category: {}", id, e);
            return ApiResponse.error("Ошибка обновления категории");
        }
    }

    @Override
    public ApiResponse<Void> deleteCategory(Long id, Long deletedBy) {
        log.info("Deleting category: {} by user: {}", id, deletedBy);

        try {
            Optional<Category> categoryOpt = categoryRepository.findById(id);
            if (categoryOpt.isEmpty()) {
                return ApiResponse.notFound("Категория с ID " + id + " не найдена");
            }

            Category category = categoryOpt.get();
            handleImageDeletion(category.getImageId());

            category.setIsActive(false);
            categoryRepository.save(category);

            log.info("🗑️ Category {} deactivated by user {}", id, deletedBy);
            return ApiResponse.successNoData();

        } catch (Exception e) {
            log.error("Error deleting category: {}", id, e);
            return ApiResponse.error("Ошибка удаления категории");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<CategoryResponseDto> getCategoryById(Long id) {
        log.debug("Getting category by ID: {}", id);

        try {
            Optional<Category> category = categoryRepository.findById(id);

            if (category.isPresent()) {
                CategoryResponseDto dto = categoryMapper.mapToResponseDto(category.get());
                return ApiResponse.success(dto);
            } else {
                return ApiResponse.notFound("Категория с ID " + id + " не найдена");
            }

        } catch (Exception e) {
            log.error("Error getting category by ID: {}", id, e);
            return ApiResponse.error("Ошибка получения категории");
        }
    }

    // ================================
    // 📋 ПОЛУЧЕНИЕ СПИСКОВ - ПОЛНАЯ ИНФОРМАЦИЯ
    // ================================

    @Override
    @Transactional(readOnly = true)
    public ListApiResponse<CategoryResponseDto> getAllActiveCategories() {
        log.debug("Getting all active categories with full info");

        try {
            List<CategoryResponseDto> categories = categoryRepository.findByIsActiveTrueOrderBySortOrderAsc()
                    .stream()
                    .map(categoryMapper::mapToResponseDto)
                    .collect(Collectors.toList());

            return ListApiResponse.success(categories);

        } catch (Exception e) {
            log.error("Error getting all active categories", e);
            return ListApiResponse.error("Ошибка получения активных категорий");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ListApiResponse<CategoryResponseDto> getAllCategories() {
        log.debug("Getting all categories (including inactive) with full info");

        try {
            List<CategoryResponseDto> categories = categoryRepository.findAllByOrderBySortOrderAsc()
                    .stream()
                    .map(categoryMapper::mapToResponseDto)
                    .collect(Collectors.toList());

            return ListApiResponse.success(categories);

        } catch (Exception e) {
            log.error("Error getting all categories", e);
            return ListApiResponse.error("Ошибка получения всех категорий");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ListApiResponse<CategoryResponseDto> searchCategories(String name) {
        log.debug("Searching categories by name: {}", name);

        try {
            List<CategoryResponseDto> categories = categoryRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name)
                    .stream()
                    .map(categoryMapper::mapToResponseDto)
                    .collect(Collectors.toList());

            return ListApiResponse.successWithMessage(categories,
                    "Найдено " + categories.size() + " категорий по запросу: " + name);

        } catch (Exception e) {
            log.error("Error searching categories by name: {}", name, e);
            return ListApiResponse.error("Ошибка поиска категорий");
        }
    }

    // ================================
    // 📋 ПОЛУЧЕНИЕ СПИСКОВ - КРАТКАЯ ИНФОРМАЦИЯ (ПРОЕКЦИИ)
    // ================================

    @Override
    @Transactional(readOnly = true)
    public ListApiResponse<CategoryBaseProjection> getActiveCategoriesBrief() {
        log.debug("Getting active categories brief");

        try {
            List<CategoryBaseProjection> categories = categoryRepository.findActiveCategoriesProjection();
            return ListApiResponse.success(categories);

        } catch (Exception e) {
            log.error("Error getting active categories brief", e);
            return ListApiResponse.error("Ошибка получения категорий");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<CategoryBaseProjection> getCategoryBrief(Long id) {
        log.debug("Getting category brief for ID: {}", id);

        try {
            Optional<CategoryBaseProjection> category = categoryRepository.findCategoryProjectionById(id);

            if (category.isPresent()) {
                return ApiResponse.success(category.get());
            } else {
                return ApiResponse.notFound("Категория с ID " + id + " не найдена");
            }

        } catch (Exception e) {
            log.error("Error getting category brief for ID: {}", id, e);
            return ApiResponse.error("Ошибка получения категории");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ListApiResponse<CategoryBaseProjection> getCategoriesBriefByIds(List<Long> ids) {
        log.debug("Getting categories brief by IDs: {}", ids);

        try {
            List<CategoryBaseProjection> categories = categoryRepository.findCategoriesProjectionByIds(ids);
            return ListApiResponse.success(categories);

        } catch (Exception e) {
            log.error("Error getting categories brief by IDs: {}", ids, e);
            return ListApiResponse.error("Ошибка получения категорий");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ListApiResponse<CategoryBaseProjection> getAllCategoriesBrief() {
        log.debug("Getting all categories brief");

        try {
            List<CategoryBaseProjection> categories = categoryRepository.findAllCategoriesProjection();
            return ListApiResponse.success(categories);

        } catch (Exception e) {
            log.error("Error getting all categories brief", e);
            return ListApiResponse.error("Ошибка получения всех категорий");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ListApiResponse<CategoryBaseProjection> searchCategoriesBrief(String name) {
        log.debug("Searching categories brief by name: {}", name);

        try {
            List<CategoryBaseProjection> categories = categoryRepository.searchActiveCategoriesProjection(name);
            return ListApiResponse.successWithMessage(categories,
                    "Найдено " + categories.size() + " категорий по запросу: " + name);

        } catch (Exception e) {
            log.error("Error searching categories brief by name: {}", name, e);
            return ListApiResponse.error("Ошибка поиска категорий");
        }
    }

    // ================================
    // 📊 СТАТИСТИКА И АНАЛИТИКА
    // ================================

    @Override
    @Transactional(readOnly = true)
    public ListApiResponse<CategoryRepository.CategoryStatsProjection> getCategoryStats() {
        log.debug("Getting category statistics");

        try {
            List<CategoryRepository.CategoryStatsProjection> stats = categoryRepository.getCategoryStatistics();
            return ListApiResponse.success(stats);

        } catch (Exception e) {
            log.error("Error getting category statistics", e);
            return ListApiResponse.error("Ошибка получения статистики категорий");
        }
    }



    // ================================
    // 🔢 ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ================================

    @Override
    @Transactional(readOnly = true)
    public Long getActiveCategoriesCount() {
        return categoryRepository.countActiveCategories();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsActiveCategoryByName(String name) {
        return categoryRepository.existsActiveCategoryByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsCategoryById(Long id) {
        return categoryRepository.existsById(id);
    }

    @Override
    public ApiResponse<CategoryResponseDto> toggleCategoryStatus(Long id, Long updatedBy) {
        log.info("Toggling category status: {} by user: {}", id, updatedBy);

        try {
            Optional<Category> categoryOpt = categoryRepository.findById(id);
            if (categoryOpt.isEmpty()) {
                return ApiResponse.notFound("Категория с ID " + id + " не найдена");
            }

            Category category = categoryOpt.get();
            Boolean currentStatus = category.getIsActive();
            category.setIsActive(!currentStatus);

            Category savedCategory = categoryRepository.save(category);
            CategoryResponseDto responseDto = categoryMapper.mapToResponseDto(savedCategory);

            String statusText = savedCategory.getIsActive() ? "активирована" : "деактивирована";
            log.info("✅ Category {} {} by user {}", id, statusText, updatedBy);

            return ApiResponse.successWithMessage(responseDto,
                    "Категория успешно " + statusText);

        } catch (Exception e) {
            log.error("Error toggling category status: {}", id, e);
            return ApiResponse.error("Ошибка изменения статуса категории");
        }
    }

    // ================================
    // 🎯 ПРИВАТНЫЕ МЕТОДЫ (БЕЗ ИЗМЕНЕНИЙ)
    // ================================

    private ImageUploadResult handleImageUpload(MultipartFile imageFile, String folder) {
        if (imageFile == null || imageFile.isEmpty()) {
            log.debug("No image file provided");
            return new ImageUploadResult(null, null);
        }

        try {
            StorageService.StorageResult storageResult = storageService.uploadImage(imageFile, folder);

            log.info("📸 Image uploaded successfully: {} with ID: {}",
                    storageResult.getUrl(), storageResult.getImageId());

            return new ImageUploadResult(storageResult.getUrl(), storageResult.getImageId());

        } catch (IOException e) {
            log.error("❌ Failed to upload image: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }

    private ImageUploadResult handleImageUpdate(MultipartFile newImageFile, String currentImageId, String folder) {
        if (newImageFile == null || newImageFile.isEmpty()) {
            log.debug("No new image provided, keeping current image");
            return new ImageUploadResult(null, null);
        }

        if (currentImageId != null) {
            handleImageDeletion(currentImageId);
        }

        return handleImageUpload(newImageFile, folder);
    }

    private void handleImageDeletion(String imageId) {
        if (imageId == null || imageId.isEmpty()) {
            log.debug("No image ID provided for deletion");
            return;
        }

        try {
            boolean deleted = storageService.deleteImage(imageId);
            log.info("🗑️ Image deletion result for ID {}: {}",
                    imageId, deleted ? "Success" : "Failed");
        } catch (Exception e) {
            log.error("❌ Error deleting image with ID {}: {}", imageId, e.getMessage(), e);
        }
    }

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