package com.example.deliveryproductservice.dto.ProductDto;

import java.math.BigDecimal;

public interface ProductBriefProjection {
    Long getId();
    String getName();
    BigDecimal getPrice();
    BigDecimal getDiscountPrice();
    String getPicUrl();
    Boolean getIsAvailable();
    BigDecimal getRating();
}