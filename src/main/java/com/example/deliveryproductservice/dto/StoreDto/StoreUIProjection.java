package com.example.deliveryproductservice.dto.StoreDto;

import java.math.BigDecimal;

public interface StoreUIProjection {
    String getName();
    String getPicUrl();
    BigDecimal getRating();
    Integer getEstimatedDeliveryTime();
}