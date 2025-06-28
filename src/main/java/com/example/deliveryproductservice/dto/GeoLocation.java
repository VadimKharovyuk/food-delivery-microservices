package com.example.deliveryproductservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class GeoLocation {
    private BigDecimal latitude;
    private BigDecimal longitude;
}