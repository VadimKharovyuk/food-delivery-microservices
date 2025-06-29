package com.example.deliveryproductservice.dto.GeoLocation;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MapboxFeature {
    private String id;
    private String type;
    private String place_name;
    private String text;
    private MapboxGeometry geometry;
    private List<String> place_type;
    private MapboxProperties properties;
    private List<MapboxContext> context;
}
