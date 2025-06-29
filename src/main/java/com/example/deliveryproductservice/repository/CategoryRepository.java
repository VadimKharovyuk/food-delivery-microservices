package com.example.deliveryproductservice.repository;

import com.example.deliveryproductservice.dto.category.CategoryBaseProjection;
import com.example.deliveryproductservice.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {


    /**
     * Получить все активные категории (полные Entity)
     */
    List<Category> findByIsActiveTrueOrderBySortOrderAsc();

    /**
     * Поиск по имени среди активных категорий (полные Entity)
     */
    List<Category> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);

    /**
     * Все категории по статусу (полные Entity)
     */
    List<Category> findByIsActiveOrderBySortOrderAsc(Boolean isActive);

    // ================================
    // 📊 МЕТОДЫ С ПРОЕКЦИЯМИ
    // ================================

    /**
     * 📋 Получить все активные категории (проекция)
     * ИСПРАВЛЕНО: убран неправильный суффикс "By"
     */
    @Query("SELECT c.id as id, c.name as name, c.isActive as isActive, c.sortOrder as sortOrder " +
            "FROM Category c WHERE c.isActive = true ORDER BY c.sortOrder ASC")
    List<CategoryBaseProjection> findActiveCategoriesProjection();

    /**
     * 🔍 Найти категорию по ID (проекция)
     */
    @Query("SELECT c.id as id, c.name as name, c.isActive as isActive, c.sortOrder as sortOrder " +
            "FROM Category c WHERE c.id = :id")
    Optional<CategoryBaseProjection> findCategoryProjectionById(@Param("id") Long id);

    /**
     * 🔍 Поиск по названию (проекция)
     */
    @Query("SELECT c.id as id, c.name as name, c.isActive as isActive, c.sortOrder as sortOrder " +
            "FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) AND c.isActive = true " +
            "ORDER BY c.sortOrder ASC")
    List<CategoryBaseProjection> searchActiveCategoriesProjection(@Param("name") String name);

    /**
     * 📊 Все категории включая неактивные (проекция)
     */
    @Query("SELECT c.id as id, c.name as name, c.isActive as isActive, c.sortOrder as sortOrder " +
            "FROM Category c ORDER BY c.sortOrder ASC, c.name ASC")
    List<CategoryBaseProjection> findAllCategoriesProjection();

    /**
     * 🔍 Категории по списку ID (проекция)
     */
    @Query("SELECT c.id as id, c.name as name, c.isActive as isActive, c.sortOrder as sortOrder " +
            "FROM Category c WHERE c.id IN :ids ORDER BY c.sortOrder ASC")
    List<CategoryBaseProjection> findCategoriesProjectionByIds(@Param("ids") List<Long> ids);

    /**
     * 🔢 Категории по диапазону сортировки (проекция)
     */
    @Query("SELECT c.id as id, c.name as name, c.isActive as isActive, c.sortOrder as sortOrder " +
            "FROM Category c WHERE c.sortOrder BETWEEN :minOrder AND :maxOrder AND c.isActive = true " +
            "ORDER BY c.sortOrder ASC")
    List<CategoryBaseProjection> findCategoriesProjectionBySortOrderRange(
            @Param("minOrder") Integer minOrder,
            @Param("maxOrder") Integer maxOrder);

    // ================================
    // 📈 СТАТИСТИКА И АНАЛИТИКА
    // ================================

    /**
     * 📊 Подсчет категорий по статусу
     */
    @Query("SELECT c.isActive as isActive, COUNT(c) as categoryCount " +
            "FROM Category c GROUP BY c.isActive")
    List<CategoryStatsProjection> getCategoryStatistics();

    /**
     * 🔢 Количество активных категорий
     */
    @Query("SELECT COUNT(c) FROM Category c WHERE c.isActive = true")
    Long countActiveCategories();

    /**
     * 🔍 Проверить существование активной категории по имени
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE LOWER(c.name) = LOWER(:name) AND c.isActive = true")
    boolean existsActiveCategoryByName(@Param("name") String name);


    /**
     * Найти все категории (включая неактивные) с полными данными, отсортированные по sortOrder
     */
    List<Category> findAllByOrderBySortOrderAsc();

    // Альтернативные варианты сортировки:

    /**
     * Найти все категории, отсортированные по дате создания (новые первые)
     */
    List<Category> findAllByOrderByCreatedAtDesc();

    /**
     * Найти все категории, отсортированные по названию
     */
    List<Category> findAllByOrderByNameAsc();


    // ================================
    // 📦 ВЛОЖЕННЫЕ ИНТЕРФЕЙСЫ ПРОЕКЦИЙ
    // ================================

    /**
     * Проекция для статистики категорий
     */
    interface CategoryStatsProjection {
        Boolean getIsActive();

        Long getCategoryCount();

        default String getStatusText() {
            return getIsActive() ? "Активные" : "Неактивные";
        }
    }


}