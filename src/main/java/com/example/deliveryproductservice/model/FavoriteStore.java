package com.example.deliveryproductservice.model;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Модель для хранения избранных ресторанов пользователей
 */
@Entity
@Table(name = "favorite_stores",
        indexes = {
                @Index(name = "idx_favorite_stores_user_id", columnList = "user_id"),
                @Index(name = "idx_favorite_stores_store_id", columnList = "store_id"),
                @Index(name = "idx_favorite_stores_user_store", columnList = "user_id, store_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_favorite_stores_user_store",
                        columnNames = {"user_id", "store_id"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "store")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FavoriteStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "user_id", nullable = false)
    @EqualsAndHashCode.Include
    private Long userId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_favorite_stores_store"))
    @EqualsAndHashCode.Include
    private Store store;

    @Column(name = "store_id", nullable = false, insertable = false, updatable = false)
    private Long storeId;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


}