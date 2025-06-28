package com.example.deliveryproductservice.repository;



import com.example.deliveryproductservice.dto.category.CategoryBaseProjection;
import com.example.deliveryproductservice.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Методы для работы с активными категориями
    List<Category> findByIsActiveTrueOrderBySortOrderAsc();

    // Поиск по имени среди активных категорий
    List<Category> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);

    // Все категории по статусу
    List<Category> findByIsActiveOrderBySortOrderAsc(Boolean isActive);

    /**
     * 📋 Получить все активные категории (краткая информация)
     */
    List<CategoryBaseProjection> findByIsActiveTrueOrderBySortOrderAscBy();

    /**
     * 🔍 Найти категорию по ID (краткая информация)
     */
    Optional<CategoryBaseProjection> findProjectionById(Long id);


    /**
     * 📊 Все категории (включая неактивные) - краткая информация
     */
    @Query("SELECT c.id as id, c.name as name, c.isActive as isActive, c.sortOrder as sortOrder " +
            "FROM Category c ORDER BY c.sortOrder ASC, c.name ASC")
    List<CategoryBaseProjection> findAllCategoriesBrief();

    /**
     * 🔍 Категории по списку ID (краткая информация)
     */
    @Query("SELECT c.id as id, c.name as name, c.isActive as isActive, c.sortOrder as sortOrder " +
            "FROM Category c WHERE c.id IN :ids")
    List<CategoryBaseProjection> findCategoriesBriefByIds(@Param("ids") List<Long> ids);

    /**
     * 📈 Статистика категорий
     */
    @Query("SELECT c.isActive as isActive, COUNT(c) as count " +
            "FROM Category c GROUP BY c.isActive")
    List<CategoryBaseProjection> getCategoryStats();

    /**
     * 🔍 Категории по списку ID (базовая проекция)
     */
    @Query("SELECT c.id as id, c.name as name, c.isActive as isActive, c.sortOrder as sortOrder " +
            "FROM Category c WHERE c.id IN :ids ORDER BY c.sortOrder ASC")
    List<CategoryBaseProjection> findCategoriesBaseByIds(@Param("ids") List<Long> ids);

}