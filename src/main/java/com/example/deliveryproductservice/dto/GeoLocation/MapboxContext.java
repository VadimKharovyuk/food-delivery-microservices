package com.example.deliveryproductservice.dto.GeoLocation;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MapboxContext {
    private String id;
    private String text;
    private String short_code;
}
