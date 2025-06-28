package com.example.deliveryproductservice.service;

import com.example.deliveryproductservice.dto.category.CategoryBaseProjection;
import com.example.deliveryproductservice.dto.category.CategoryResponseDto;
import com.example.deliveryproductservice.dto.category.CreateCategoryDto;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    CategoryResponseDto createCategory(CreateCategoryDto dto, Long createdBy) ;

    List<CategoryResponseDto> getAllActiveCategories();

    void deleteCategory(Long id, Long deletedBy);
    CategoryResponseDto getCategoryById(Long id);

    CategoryResponseDto updateCategory(Long id, CreateCategoryDto dto, Long updatedBy);


    List<CategoryBaseProjection> getActiveCategoriesBrief();
    Optional<CategoryBaseProjection> getCategoryBrief(Long id);

    List<CategoryBaseProjection> getCategoriesBriefByIds(List<Long> ids);
}
