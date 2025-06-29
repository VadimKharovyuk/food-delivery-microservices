package com.example.deliveryproductservice.dto.StoreDto;

import com.example.deliveryproductservice.dto.StoreDto.StoreResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreUIDto {
    private String name;
    private String picUrl;
    private BigDecimal rating;
    private Integer estimatedDeliveryTime;

}