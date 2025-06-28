package com.example.deliveryproductservice.repository;



import com.example.deliveryproductservice.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Методы для работы с активными категориями
    List<Category> findByIsActiveTrueOrderBySortOrderAsc();

    // Поиск по имени среди активных категорий
    List<Category> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);

    // Все категории по статусу
    List<Category> findByIsActiveOrderBySortOrderAsc(Boolean isActive);
}