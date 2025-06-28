package com.example.deliveryproductservice.dto.StoreDto;


import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

/**
 * DTO для обновления магазина (без изменения адреса)
 */
@Data
public class UpdateStoreDto {

    @Size(max = 100, message = "Store name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Min(value = 1, message = "Delivery radius must be at least 1 km")
    @Max(value = 50, message = "Delivery radius cannot exceed 50 km")
    private Integer deliveryRadius;

    @DecimalMin(value = "0.00", message = "Delivery fee cannot be negative")
    @DecimalMax(value = "99999.99", message = "Delivery fee is too high")
    private BigDecimal deliveryFee;

    @Min(value = 10, message = "Delivery time must be at least 10 minutes")
    @Max(value = 180, message = "Delivery time cannot exceed 180 minutes")
    private Integer estimatedDeliveryTime;

    private MultipartFile imageFile;

    private Boolean isActive;
}

