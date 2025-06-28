package com.example.deliveryproductservice.service;

import com.example.deliveryproductservice.dto.category.CategoryResponseDto;
import com.example.deliveryproductservice.dto.category.CreateCategoryDto;
import com.example.deliveryproductservice.model.Category;
import com.example.deliveryproductservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponseDto createCategory(CreateCategoryDto dto, Long createdBy) {
        log.info("Creating new category: {} by user: {}", dto.getName(), createdBy);

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setImageUrl(dto.getImageUrl());
        category.setIsActive(dto.getIsActive());
        category.setSortOrder(dto.getSortOrder());

        Category savedCategory = categoryRepository.save(category);

        log.info("Category created with ID: {}", savedCategory.getId());
        return mapToResponseDto(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }


    private CategoryResponseDto mapToResponseDto(Category category) {
        CategoryResponseDto dto = new CategoryResponseDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setImageUrl(category.getImageUrl());
        dto.setIsActive(category.getIsActive());
        dto.setSortOrder(category.getSortOrder());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }
}
