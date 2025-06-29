package com.example.deliveryproductservice.dto.StoreDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreBriefDto {
    private Long id;
    private String name;
    private Boolean isActive;
    private BigDecimal rating;
    private BigDecimal deliveryFee;
    private Integer estimatedDeliveryTime;
    private String picUrl;
    private String addressCity;
    private String addressStreet;

    // Метод для создания из проекции
    public static StoreBriefDto fromProjection(StoreBriefProjection projection) {
        return StoreBriefDto.builder()
                .id(projection.getId())
                .name(projection.getName())
                .isActive(projection.getIsActive())
                .rating(projection.getRating())
                .deliveryFee(projection.getDeliveryFee())
                .estimatedDeliveryTime(projection.getEstimatedDeliveryTime())
                .picUrl(projection.getPicUrl())
                .addressCity(projection.getAddressCity())
                .addressStreet(projection.getAddressStreet())
                .build();
    }
}