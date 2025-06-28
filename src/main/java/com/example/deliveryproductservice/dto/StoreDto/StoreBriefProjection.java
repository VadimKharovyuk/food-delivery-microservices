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

}