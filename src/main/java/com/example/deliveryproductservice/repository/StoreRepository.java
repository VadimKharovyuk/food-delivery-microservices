package com.example.deliveryproductservice.repository;

import com.example.deliveryproductservice.model.Store;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
