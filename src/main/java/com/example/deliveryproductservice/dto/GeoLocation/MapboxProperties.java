package com.example.deliveryproductservice.dto.GeoLocation;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MapboxProperties {
    private String accuracy;
    private String address;
    private String category;
    private String maki;
}