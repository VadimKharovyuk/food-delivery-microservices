package com.example.deliveryproductservice.dto.StoreDto;


import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Проекция для владельцев магазинов (расширенная информация)
 */
public interface StoreOwnerProjection {
    Long getId();
    String getName();
    String getDescription();
    Boolean getIsActive();
    BigDecimal getRating();
    BigDecimal getDeliveryFee();
    Integer getEstimatedDeliveryTime();
    String getPicUrl();
    LocalDateTime getCreatedAt();

    // Адрес
    String getAddressCity();
    String getAddressStreet();



    default String getAgeText() {
        return "Работает с " + getCreatedAt().toLocalDate();
    }
}

