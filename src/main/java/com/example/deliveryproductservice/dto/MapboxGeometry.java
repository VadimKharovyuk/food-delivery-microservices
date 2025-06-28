package com.example.deliveryproductservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MapboxGeometry {
    private String type;
    private double[] coordinates; // [longitude, latitude]
}
