package com.example.deliveryproductservice.dto.StoreDto;

import java.math.BigDecimal;

public interface StoreBriefProjection {
    Long getId();
    String getName();
    Boolean getIsActive();
    BigDecimal getRating();
    BigDecimal getDeliveryFee();
    Integer getEstimatedDeliveryTime();
    String getPicUrl();

    // Адрес - через вложенные свойства
    String getAddressCity();
    String getAddressStreet();
}
