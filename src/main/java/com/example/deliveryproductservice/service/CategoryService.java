package com.example.deliveryproductservice.service;

import com.example.deliveryproductservice.dto.category.*;

import java.util.List;
import java.util.Optional;


public interface CategoryService {


    CategoryResponseDto createCategory(CreateCategoryDto dto, Long createdBy);


    CategoryResponseDto updateCategory(Long id, CreateCategoryDto dto, Long updatedBy);
    CategoryStatsResponseWrapper getCategoryStats();

    void deleteCategory(Long id, Long deletedBy);

    // ================================
    // 🔍 ПОЛУЧЕНИЕ ПОЛНОЙ ИНФОРМАЦИИ
    // ================================

    CategoryBriefResponseWrapper getActiveCategoriesBrief() ;
    CategoriesResponseWrapper getAllActiveCategories();

    /**
     * 🔍 Получить категорию по ID (полная информация)
     *
     * Возвращает полную информацию о категории по указанному ID.
     * Включает все поля, даже если категория неактивна.
     *
     * @param id ID категории
     * @return полная информация о категории
     * @throws RuntimeException если категория не найдена
     */
    CategoryResponseDto getCategoryById(Long id);

    // ================================
    // 📊 ЛЕГКОВЕСНЫЕ ПРОЕКЦИИ
    // ================================

    /**
     * 🔍 Получить краткую информацию о категории по ID
     *
     * Возвращает только основные поля категории (ID, название, статус, порядок).
     * Может вернуть и неактивную категорию, если она существует.
     * Подходит для быстрой проверки существования и базовой информации.
     *
     * @param id ID категории
     * @return краткая информация о категории или Optional.empty() если не найдена
     */
    Optional<CategoryBaseProjection> getCategoryBrief(Long id);

    /**
     * 🔍 Получить краткую информацию о категориях по списку ID
     *
     * Возвращает краткую информацию только для найденных категорий.
     * Если какие-то ID не существуют, они просто не включаются в результат.
     * Полезно для:
     * - Получения информации о избранных категориях
     * - Валидации списка категорий
     * - Массовых операций
     *
     * @param ids список ID категорий для получения
     * @return список найденных категорий с краткой информацией
     */
    List<CategoryBaseProjection> getCategoriesBriefByIds(List<Long> ids);
}