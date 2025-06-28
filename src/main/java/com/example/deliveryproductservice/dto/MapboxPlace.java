package com.example.deliveryproductservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapboxPlace {
    private String name;
    private String fullName;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
