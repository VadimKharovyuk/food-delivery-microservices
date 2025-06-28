package com.example.deliveryproductservice.dto.StoreDto;

import java.math.BigDecimal;

public interface StoreBriefProjection {
    Long getId();
    String getName();
    Boolean getIsActive();
    BigDecimal getRating();
    Integer getDeliveryRadius();
    BigDecimal getDeliveryFee();
    Integer getEstimatedDeliveryTime();
    String getPicUrl();

    // Адрес
    String getAddressCity();
    String getAddressStreet();
    BigDecimal getAddressLatitude();
    BigDecimal getAddressLongitude();

    // Вычисляемые поля
    default String getDeliveryInfo() {
        return String.format("$%.2f • %d-%d min",
                getDeliveryFee(),
                getEstimatedDeliveryTime() - 5,
                getEstimatedDeliveryTime() + 5);
    }

    default String getRatingDisplay() {
        return getRating() != null ? String.format("⭐ %.1f", getRating()) : "⭐ New";
    }
}