package com.example.deliveryproductservice.service;

import com.example.deliveryproductservice.dto.category.CategoryBaseProjection;
import com.example.deliveryproductservice.dto.category.CategoryResponseDto;
import com.example.deliveryproductservice.dto.category.CreateCategoryDto;

import java.util.List;
import java.util.Optional;


public interface CategoryService {


    CategoryResponseDto createCategory(CreateCategoryDto dto, Long createdBy);


    CategoryResponseDto updateCategory(Long id, CreateCategoryDto dto, Long updatedBy);


    void deleteCategory(Long id, Long deletedBy);

    // ================================
    // 🔍 ПОЛУЧЕНИЕ ПОЛНОЙ ИНФОРМАЦИИ
    // ================================

    /**
     * 📋 Получить все активные категории (полная информация)
     *
     * Возвращает список всех активных категорий с полной информацией включая:
     * - Все поля категории
     * - URL изображения
     * - Даты создания и обновления
     * Отсортированы по sortOrder, затем по названию.
     *
     * @return список активных категорий с полной информацией
     */
    List<CategoryResponseDto> getAllActiveCategories();

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
     * 📊 Получить краткий список активных категорий
     *
     * Возвращает только основные поля (ID, название, статус, порядок) для активных категорий.
     * Идеально подходит для:
     * - Dropdown меню
     * - Селекторы
     * - Быстрые списки
     * - Мобильные приложения
     *
     * @return краткий список активных категорий
     */
    List<CategoryBaseProjection> getActiveCategoriesBrief();

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