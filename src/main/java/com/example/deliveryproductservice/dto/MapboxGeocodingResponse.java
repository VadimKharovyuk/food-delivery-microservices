package com.example.deliveryproductservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Модели для Mapbox API Response
@Data
@NoArgsConstructor
public class MapboxGeocodingResponse {
    private String type;
    private List<MapboxFeature> features;
    private String attribution;
}
