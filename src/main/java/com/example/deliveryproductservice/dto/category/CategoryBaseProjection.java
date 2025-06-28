package com.example.deliveryproductservice.dto.category;

public interface CategoryBaseProjection {
    Long getId();

    String getName();

    Boolean getIsActive();

    Integer getSortOrder();
}
