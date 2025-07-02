package com.example.deliveryproductservice.repository;

import com.example.deliveryproductservice.model.FavoriteStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteStoreRepository extends JpaRepository<FavoriteStore, Long> {

    // ================================
    // ОСНОВНЫЕ ЗАПРОСЫ
    // ================================

    /**
     * Найти все избранные рестораны пользователя с полной информацией о ресторане
     * @param userId ID пользователя
     * @return список избранных ресторанов с загруженной информацией о ресторане
     */
    @Query("SELECT f FROM FavoriteStore f " +
            "JOIN FETCH f.store s " +
            "WHERE f.userId = :userId " +
            "ORDER BY f.createdAt DESC")
    List<FavoriteStore> findByUserIdWithStore(@Param("userId") Long userId);

    /**
     * Найти конкретную запись избранного ресторана с информацией о ресторане
     * @param userId ID пользователя
     * @param storeId ID ресторана
     * @return запись избранного ресторана с полной информацией
     */
    @Query("SELECT f FROM FavoriteStore f " +
            "JOIN FETCH f.store s " +
            "WHERE f.userId = :userId AND s.id = :storeId")
    Optional<FavoriteStore> findByUserIdAndStoreId(@Param("userId") Long userId,
                                                   @Param("storeId") Long storeId);

    /**
     * Найти только активные избранные рестораны пользователя
     * @param userId ID пользователя
     * @return список активных избранных ресторанов
     */
    @Query("SELECT f FROM FavoriteStore f " +
            "JOIN FETCH f.store s " +
            "WHERE f.userId = :userId AND s.isActive = true " +
            "ORDER BY f.createdAt DESC")
    List<FavoriteStore> findActiveByUserId(@Param("userId") Long userId);

    // ================================
    // МЕТОДЫ БЕЗ JOIN FETCH (для простых проверок)
    // ================================

    /**
     * Проверить, есть ли ресторан в избранном у пользователя (без загрузки Store)
     * @param userId ID пользователя
     * @param storeId ID ресторана
     * @return true если ресторан в избранном
     */
    boolean existsByUserIdAndStoreId(Long userId, Long storeId);

    /**
     * Подсчитать количество избранных ресторанов у пользователя
     * @param userId ID пользователя
     * @return количество избранных ресторанов
     */
    long countByUserId(Long userId);

    /**
     * Подсчитать сколько раз ресторан добавлен в избранное
     * @param storeId ID ресторана
     * @return количество добавлений в избранное
     */
    @Query("SELECT COUNT(f) FROM FavoriteStore f WHERE f.store.id = :storeId")
    long countByStoreId(@Param("storeId") Long storeId);

    // ================================
    // ДОПОЛНИТЕЛЬНЫЕ ПОЛЕЗНЫЕ ЗАПРОСЫ
    // ================================

    /**
     * Найти избранные рестораны пользователя по городу
     * @param userId ID пользователя
     * @param city город
     * @return список избранных ресторанов в указанном городе
     */
    @Query("SELECT f FROM FavoriteStore f " +
            "JOIN FETCH f.store s " +
            "WHERE f.userId = :userId AND s.address.city = :city " +
            "ORDER BY f.createdAt DESC")
    List<FavoriteStore> findByUserIdAndCity(@Param("userId") Long userId,
                                            @Param("city") String city);

    /**
     * Найти избранные рестораны с рейтингом выше указанного
     * @param userId ID пользователя
     * @param minRating минимальный рейтинг
     * @return список ресторанов с высоким рейтингом
     */
    @Query("SELECT f FROM FavoriteStore f " +
            "JOIN FETCH f.store s " +
            "WHERE f.userId = :userId AND s.rating >= :minRating " +
            "ORDER BY s.rating DESC, f.createdAt DESC")
    List<FavoriteStore> findByUserIdAndMinRating(@Param("userId") Long userId,
                                                 @Param("minRating") java.math.BigDecimal minRating);

    /**
     * Получить последние N избранных ресторанов пользователя
     * @param userId ID пользователя
     * @param limit количество записей
     * @return список последних избранных ресторанов
     */
    @Query(value = "SELECT f FROM FavoriteStore f " +
            "JOIN FETCH f.store s " +
            "WHERE f.userId = :userId " +
            "ORDER BY f.createdAt DESC")
    List<FavoriteStore> findTopByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId,
                                                            org.springframework.data.domain.Pageable pageable);

    // ================================
    // СТАТИСТИЧЕСКИЕ ЗАПРОСЫ
    // ================================

    /**
     * Получить статистику избранного по пользователю
     * @param userId ID пользователя
     * @return массив [activeCount, inactiveCount]
     */
    @Query("SELECT " +
            "COUNT(CASE WHEN s.isActive = true THEN 1 END) as activeCount, " +
            "COUNT(CASE WHEN s.isActive = false THEN 1 END) as inactiveCount " +
            "FROM FavoriteStore f " +
            "JOIN f.store s " +
            "WHERE f.userId = :userId")
    Object[] getFavoriteStatsForUser(@Param("userId") Long userId);

    /**
     * Найти пользователей, которые добавили определенный ресторан в избранное
     * @param storeId ID ресторана
     * @return список ID пользователей
     */
    @Query("SELECT DISTINCT f.userId FROM FavoriteStore f WHERE f.store.id = :storeId")
    List<Long> findUserIdsByStoreId(@Param("storeId") Long storeId);

    // ================================
    // ОПЕРАЦИИ УДАЛЕНИЯ
    // ================================

    /**
     * Удалить ресторан из избранного (используйте в @Modifying транзакциях)
     * @param userId ID пользователя
     * @param storeId ID ресторана
     * @return количество удаленных записей
     */
    @Query("DELETE FROM FavoriteStore f WHERE f.userId = :userId AND f.store.id = :storeId")
    int deleteByUserIdAndStoreId(@Param("userId") Long userId, @Param("storeId") Long storeId);
}