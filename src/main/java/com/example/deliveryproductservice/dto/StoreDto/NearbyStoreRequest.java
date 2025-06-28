package com.example.deliveryproductservice.dto.StoreDto;


import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Параметры поиска магазинов поблизости
 */
@Data
public class NearbyStoreRequest {

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Invalid latitude")
    @DecimalMax(value = "90.0", message = "Invalid latitude")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Invalid longitude")
    @DecimalMax(value = "180.0", message = "Invalid longitude")
    private BigDecimal longitude;

    @Min(value = 1, message = "Radius must be at least 1 km")
    @Max(value = 50, message = "Radius cannot exceed 50 km")
    private Integer radiusKm = 10; // По умолчанию 10 км

    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = 100, message = "Limit cannot exceed 100")
    private Integer limit = 20; // По умолчанию 20 магазинов

    private String sortBy = "distance"; // distance, rating, deliveryTime, deliveryFee

    // Фильтры
    private BigDecimal maxDeliveryFee;
    private Integer maxDeliveryTime;
    private BigDecimal minRating;
}
