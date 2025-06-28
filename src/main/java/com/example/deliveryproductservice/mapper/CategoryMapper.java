package com.example.deliveryproductservice.mapper;

import com.example.deliveryproductservice.dto.category.CategoryResponseDto;
import com.example.deliveryproductservice.dto.category.CreateCategoryDto;
import com.example.deliveryproductservice.model.Category;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class CategoryMapper {


    public CategoryResponseDto mapToResponseDto(Category category) {
        if (category == null) {
            return null;
        }

        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setImageUrl(category.getImageUrl());
        dto.setIsActive(category.getIsActive());
        dto.setSortOrder(category.getSortOrder());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());

        log.debug("Mapped Category {} to ResponseDto", category.getId());
        return dto;
    }


    public Category mapToEntity(CreateCategoryDto dto, String imageUrl) {
        if (dto == null) {
            return null;
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setImageUrl(imageUrl); // URL после загрузки в Cloudinary
        category.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);

        log.debug("Mapped CreateCategoryDto to Category entity");
        return category;
    }


    public void updateEntityFromDto(Category category, CreateCategoryDto dto, String imageUrl) {
        if (category == null || dto == null) {
            return;
        }

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        // Обновляем URL только если предоставлен новый
        if (imageUrl != null) {
            category.setImageUrl(imageUrl);
        }

        category.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : category.getIsActive());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : category.getSortOrder());

        log.debug("Updated Category {} from DTO", category.getId());
    }
}

