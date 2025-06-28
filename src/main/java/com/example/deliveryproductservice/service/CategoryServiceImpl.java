package com.example.deliveryproductservice.service;

import com.example.deliveryproductservice.dto.category.CategoryResponseDto;
import com.example.deliveryproductservice.dto.category.CreateCategoryDto;
import com.example.deliveryproductservice.mapper.CategoryMapper;
import com.example.deliveryproductservice.model.Category;
import com.example.deliveryproductservice.repository.CategoryRepository;
import com.example.deliveryproductservice.service.CategoryService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final StorageService storageService;

    @Override
    public CategoryResponseDto createCategory(CreateCategoryDto dto, Long createdBy) {
        log.info("Creating new category: {} by user: {}", dto.getName(), createdBy);

        ImageUploadResult imageResult = handleImageUpload(dto.getImageFile(), "categories");

        Category category = categoryMapper.mapToEntity(dto, imageResult.getImageUrl());

        if (imageResult.getImageId() != null) {
            category.setImageId(imageResult.getImageId());
        }
        Category savedCategory = categoryRepository.save(category);

        log.info("✅ Category created with ID: {} and image: {}",
                savedCategory.getId(),
                imageResult.getImageUrl() != null ? "Yes" : "No");

        return categoryMapper.mapToResponseDto(savedCategory);
    }

    @Override
    public CategoryResponseDto updateCategory(Long id, CreateCategoryDto dto, Long updatedBy) {
        log.info("Updating category: {} by user: {}", id, updatedBy);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // 📸 Обрабатываем изображение (загрузка нового и удаление старого)
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

    @Override
    public void deleteCategory(Long id, Long deletedBy) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        handleImageDeletion(category.getImageId());

        category.setIsActive(false);
        categoryRepository.save(category);

        log.info("🗑️ Category {} deactivated by user {}", id, deletedBy);
    }

    // 🎯 ПРИВАТНЫЕ МЕТОДЫ ДЛЯ РАБОТЫ С ИЗОБРАЖЕНИЯМИ

    /**
     * 📸 Загрузка нового изображения
     */
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
        // Если новое изображение не предоставлено, оставляем текущее
        if (newImageFile == null || newImageFile.isEmpty()) {
            log.debug("No new image provided, keeping current image");
            return new ImageUploadResult(null, null); // null означает "не обновлять"
        }

        // Удаляем старое изображение
        if (currentImageId != null) {
            handleImageDeletion(currentImageId);
        }

        // Загружаем новое изображение
        return handleImageUpload(newImageFile, folder);
    }

    /**
     * 🗑️ Удаление изображения
     */
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
            // Не выбрасываем исключение, чтобы не прерывать основную операцию
        }
    }



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

