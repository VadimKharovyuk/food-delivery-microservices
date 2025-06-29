package com.example.deliveryproductservice.repository;

import com.example.deliveryproductservice.dto.StoreDto.StoreBriefProjection;
import com.example.deliveryproductservice.dto.StoreDto.StoreUIProjection;
import com.example.deliveryproductservice.model.Store;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    /**
     * Проверка существования активного магазина с таким названием у владельца
     */
    boolean existsByNameAndOwnerIdAndIsActiveTrue(String name, Long ownerId);

    // Для бесконечной прокрутки
    Slice<Store> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    // По владельцу
    Slice<Store> findByOwnerIdAndIsActiveTrueOrderByCreatedAtDesc(Long ownerId, Pageable pageable);

    // Поиск по названию
    Slice<Store> findByNameContainingIgnoreCaseAndIsActiveTrueOrderByCreatedAtDesc(String name, Pageable pageable);


    Optional<Store> findByIdAndIsActiveTrue(Long storeId);

    // Interface-based проекция - Spring автоматически создаст прокси
    Slice<StoreUIProjection> findByIsActiveTrueOrderByRatingDescCreatedAtDesc(Pageable pageable);


    //  краткая проекция (9 полей)
    @Query("SELECT s.id as id, s.name as name, s.isActive as isActive, " +
            "s.rating as rating, s.deliveryFee as deliveryFee, " +
            "s.estimatedDeliveryTime as estimatedDeliveryTime, s.picUrl as picUrl, " +
            "s.address.city as addressCity, s.address.street as addressStreet " +
            "FROM Store s WHERE s.isActive = true ORDER BY s.createdAt DESC")
    Slice<StoreBriefProjection> findActiveStoresBrief(Pageable pageable);

}
