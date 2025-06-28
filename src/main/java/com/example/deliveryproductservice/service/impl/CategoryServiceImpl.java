package com.example.deliveryproductservice.service.impl;
import com.example.deliveryproductservice.dto.category.CategoryBaseProjection;
import com.example.deliveryproductservice.dto.category.CategoryResponseDto;
import com.example.deliveryproductservice.dto.category.CreateCategoryDto;
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
    // 📊 МЕТОДЫ С ПРОЕКЦИЯМИ (ОБНОВЛЕНЫ)
    // ================================

    @Override
    @Transactional(readOnly = true)
    public List<CategoryBaseProjection> getActiveCategoriesBrief() {
        log.debug("Getting active categories brief");
        return categoryRepository.findActiveCategoriesProjection(); // ИСПРАВЛЕНО
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryBaseProjection> getCategoryBrief(Long id) {
        log.debug("Getting category brief for ID: {}", id);
        return categoryRepository.findCategoryProjectionById(id); // ИСПРАВЛЕНО
    }

    @Transactional(readOnly = true)
    public List<CategoryBaseProjection> getCategoriesBriefByIds(List<Long> ids) {
        log.debug("Getting categories brief by IDs: {}", ids);
        return categoryRepository.findCategoriesProjectionByIds(ids); // ИСПРАВЛЕНО
    }

    /**
     * 📋 Все категории (включая неактивные) - проекция
     */
    @Transactional(readOnly = true)
    public List<CategoryBaseProjection> getAllCategoriesBrief() {
        log.debug("Getting all categories brief");
        return categoryRepository.findAllCategoriesProjection();
    }

    /**
     * 🔍 Поиск категорий по названию - проекция
     */
    @Transactional(readOnly = true)
    public List<CategoryBaseProjection> searchCategoriesBrief(String name) {
        log.debug("Searching categories brief by name: {}", name);
        return categoryRepository.searchActiveCategoriesProjection(name);
    }

    /**
     * 🔢 Категории по диапазону сортировки
     */
    @Transactional(readOnly = true)
    public List<CategoryBaseProjection> getCategoriesBriefBySortRange(Integer minOrder, Integer maxOrder) {
        log.debug("Getting categories brief by sort order range: {} - {}", minOrder, maxOrder);
        return categoryRepository.findCategoriesProjectionBySortOrderRange(minOrder, maxOrder);
    }

    /**
     * 📊 Статистика категорий
     */
    @Transactional(readOnly = true)
    public List<CategoryRepository.CategoryStatsProjection> getCategoryStats() {
        log.debug("Getting category statistics");
        return categoryRepository.getCategoryStatistics();
    }

    /**
     * 🔢 Количество активных категорий
     */
    @Transactional(readOnly = true)
    public Long getActiveCategoriesCount() {
        return categoryRepository.countActiveCategories();
    }

    /**
     * 🔍 Проверить существование активной категории по имени
     */
    @Transactional(readOnly = true)
    public boolean existsActiveCategoryByName(String name) {
        return categoryRepository.existsActiveCategoryByName(name);
    }

    // ================================
    // 🔍 МЕТОДЫ С ПОЛНОЙ ИНФОРМАЦИЕЙ (БЕЗ ИЗМЕНЕНИЙ)
    // ================================

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(categoryMapper::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return categoryMapper.mapToResponseDto(category);
    }

    // ================================
    // ✏️ CRUD ОПЕРАЦИИ (БЕЗ ИЗМЕНЕНИЙ)
    // ================================

    @Override
    public CategoryResponseDto createCategory(CreateCategoryDto dto, Long createdBy) {
        log.info("Creating new category: {} by user: {}", dto.getName(), createdBy);

        // Проверяем уникальность имени
        if (existsActiveCategoryByName(dto.getName())) {
            throw new RuntimeException("Category with name '" + dto.getName() + "' already exists");
        }

        ImageUploadResult imageResult = handleImageUpload(dto.getImageFile(), "categories");

        Category category = categoryMapper.mapToEntity(dto, imageResult.getImageUrl());

        if (imageResult.getImageId() != null) {
            category.setImageId(imageResult.getImageId());
        }
        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.mapToResponseDto(savedCategory);
    }

    @Override
    public CategoryResponseDto updateCategory(Long id, CreateCategoryDto dto, Long updatedBy) {
        log.info("Updating category: {} by user: {}", id, updatedBy);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Проверяем уникальность имени (если имя изменилось)
        if (!category.getName().equals(dto.getName()) && existsActiveCategoryByName(dto.getName())) {
            throw new RuntimeException("Category with name '" + dto.getName() + "' already exists");
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

        log.info("✅ Category updated: {}", savedCategory.getId());
        return categoryMapper.mapToResponseDto(savedCategory);
    }

    @Override
    public void deleteCategory(Long id, Long deletedBy) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        handleImageDeletion(category.getImageId());

        category.setIsActive(false);
        categoryRepository.save(category);

        log.info("🗑️ Category {} deactivated by user {}", id, deletedBy);
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