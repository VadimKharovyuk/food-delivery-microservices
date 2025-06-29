package com.example.deliveryproductservice.service;
import com.example.deliveryproductservice.dto.category.*;
import com.example.deliveryproductservice.repository.CategoryRepository;
import java.util.List;
public interface CategoryService {


    ApiResponse<CategoryResponseDto> createCategory(CreateCategoryDto dto, Long createdBy);

    /**
     * ✏️ Обновить категорию
     */
    ApiResponse<CategoryResponseDto> updateCategory(Long id, CreateCategoryDto dto, Long updatedBy);

    /**
     * 🗑️ Удалить категорию (деактивировать)
     */
    ApiResponse<Void> deleteCategory(Long id, Long deletedBy);

    /**
     * 🔍 Получить категорию по ID - полная информация
     */
    ApiResponse<CategoryResponseDto> getCategoryById(Long id);


    /**
     * 📋 Все активные категории - полная информация
     */
    ListApiResponse<CategoryResponseDto> getAllActiveCategories();

    /**
     * 📋 Все категории (включая неактивные) - полная информация
     */
    ListApiResponse<CategoryResponseDto> getAllCategories();

    /**
     * 🔍 Поиск категорий по названию - полная информация
     */
    ListApiResponse<CategoryResponseDto> searchCategories(String name);

    // ================================
    // 📋 ПОЛУЧЕНИЕ СПИСКОВ - КРАТКАЯ ИНФОРМАЦИЯ (ПРОЕКЦИИ)
    // ================================

    /**
     * 📋 Активные категории - краткая информация
     */
    ListApiResponse<CategoryBaseProjection> getActiveCategoriesBrief();

    /**
     * 🔍 Одна категория - краткая информация
     */
    ApiResponse<CategoryBaseProjection> getCategoryBrief(Long id);

    /**
     * 📋 Категории по списку ID - краткая информация
     */
    ListApiResponse<CategoryBaseProjection> getCategoriesBriefByIds(List<Long> ids);

    /**
     * 📋 Все категории (включая неактивные) - краткая информация
     */
    ListApiResponse<CategoryBaseProjection> getAllCategoriesBrief();

    /**
     * 🔍 Поиск категорий по названию - краткая информация
     */
    ListApiResponse<CategoryBaseProjection> searchCategoriesBrief(String name);

    // ================================
    // 📊 СТАТИСТИКА И АНАЛИТИКА
    // ================================

    /**
     * 📊 Статистика категорий
     */
    ListApiResponse<CategoryRepository.CategoryStatsProjection> getCategoryStats();

    // ================================
    // 🔢 ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ================================

    /**
     * 🔢 Количество активных категорий
     */
    Long getActiveCategoriesCount();

    /**
     * ✅ Проверить существование активной категории по имени
     */
    boolean existsActiveCategoryByName(String name);

    /**
     * ✅ Проверить существование категории по ID
     */
    boolean existsCategoryById(Long id);

    /**
     * 🔄 Активировать/деактивировать категорию
     */
    ApiResponse<CategoryResponseDto> toggleCategoryStatus(Long id, Long updatedBy);
}

