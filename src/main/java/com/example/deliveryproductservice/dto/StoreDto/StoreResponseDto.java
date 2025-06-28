package com.example.deliveryproductservice.dto.StoreDto;

import com.example.deliveryproductservice.model.Address;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class StoreResponseDto {
    private Long id;
    private Long ownerId;
    private String name;
    private String description;
    private Address address;
    private String phone;
    private String email;
    private Boolean isActive;
    private BigDecimal rating;
    private Integer deliveryRadius;
    private BigDecimal deliveryFee;
    private Integer estimatedDeliveryTime;
    private String picUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Вычисляемые поля
    public String getDeliveryInfo() {
        return String.format("$%.2f • %d-%d min",
                deliveryFee,
                estimatedDeliveryTime - 5,
                estimatedDeliveryTime + 5);
    }

    public String getRatingDisplay() {
        return rating != null ? String.format("⭐ %.1f", rating) : "⭐ New";
    }
}
