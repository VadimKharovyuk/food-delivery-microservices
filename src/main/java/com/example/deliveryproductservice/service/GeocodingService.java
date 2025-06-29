package com.example.deliveryproductservice.service;

import com.example.deliveryproductservice.dto.GeoLocation.CreateAddressRequest;
import com.example.deliveryproductservice.dto.GeoLocation.MapboxPlace;
import com.example.deliveryproductservice.model.Address;

import java.math.BigDecimal;
import java.util.List;

public interface GeocodingService {
    Address createAddressWithCoordinates(CreateAddressRequest request);
    String reverseGeocode(BigDecimal longitude, BigDecimal latitude);
    List<MapboxPlace> searchNearbyPlaces(BigDecimal longitude, BigDecimal latitude, String query, int limit);
}
