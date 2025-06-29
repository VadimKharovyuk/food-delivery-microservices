package com.example.deliveryproductservice.repository;

import com.example.deliveryproductservice.dto.ProductDto.ProductBriefProjection;
import com.example.deliveryproductservice.model.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Все доступные продукты с пагинацией
    Slice<Product> findByIsAvailableTrueOrderByCreatedAtDesc(Pageable pageable);

    // Продукты конкретного магазина
    Slice<Product> findByStoreIdAndIsAvailableTrueOrderByCreatedAtDesc(Long storeId, Pageable pageable);

    // Продукты конкретной категории
    Slice<Product> findByCategoryIdAndIsAvailableTrueOrderByCreatedAtDesc(Long categoryId, Pageable pageable);

    // Поиск по названию
    Slice<Product> findByNameContainingIgnoreCaseAndIsAvailableTrueOrderByCreatedAtDesc(String name, Pageable pageable);

    // Один продукт по ID
    Optional<Product> findByIdAndIsAvailableTrue(Long id);

    // Проекция для кратких данных
    @Query("SELECT p.id as id, p.name as name, p.price as price, " +
            "p.discountPrice as discountPrice, p.picUrl as picUrl, " +
            "p.isAvailable as isAvailable, p.rating as rating " +
            "FROM Product p WHERE p.storeId = :storeId AND p.isAvailable = true " +
            "ORDER BY p.createdAt DESC")
    Slice<ProductBriefProjection> findProductsBriefByStore(@Param("storeId") Long storeId, Pageable pageable);
}