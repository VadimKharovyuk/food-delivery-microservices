package com.example.deliveryproductservice.service;

import com.example.deliveryproductservice.dto.GeoLocation.*;
import com.example.deliveryproductservice.model.Address;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapboxGeocodingService implements GeocodingService {

    @Value("${mapbox.access.token}")
    private String mapboxToken;

    private final RestTemplate restTemplate;
    private static final String MAPBOX_GEOCODING_URL = "https://api.mapbox.com/geocoding/v5/mapbox.places";

    public MapboxGeocodingService() {
        this.restTemplate = new RestTemplate();
    }

    public Address createAddressWithCoordinates(CreateAddressRequest request) {
        Address.AddressBuilder builder = Address.builder()
                .street(request.getStreet())
                .city(request.getCity())
                .region(request.getRegion())
                .country(request.getCountry())
                .postalCode(request.getPostalCode());

        // Если координаты переданы напрямую
        if (request.getLatitude() != null && request.getLongitude() != null) {
            builder.latitude(request.getLatitude())
                    .longitude(request.getLongitude());
            log.info("Using provided coordinates: {}, {}", request.getLatitude(), request.getLongitude());
        }
        // Иначе получаем через Mapbox geocoding
        else if (request.getAutoGeocode()) {
            try {
                String address = getFormattedAddress(request);
                GeoLocation coordinates = geocodeAddress(address);
                builder.latitude(coordinates.getLatitude())
                        .longitude(coordinates.getLongitude())
                        .fullAddress(address);
                log.info("Geocoded address '{}' to coordinates: {}, {}",
                        address, coordinates.getLatitude(), coordinates.getLongitude());
            } catch (Exception e) {
                log.error("Geocoding failed for address: {}", getFormattedAddress(request), e);
                throw new RuntimeException("Could not determine coordinates for address: " + e.getMessage());
            }
        } else {
            throw new RuntimeException("Coordinates must be provided or geocoding must be enabled");
        }

        return builder.build();
    }

    /**
     * Геокодирование адреса через Mapbox Geocoding API
     */
    public GeoLocation geocodeAddress(String address) {
        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = String.format("%s/%s.json?access_token=%s&limit=1&types=address,poi",
                    MAPBOX_GEOCODING_URL, encodedAddress, mapboxToken);

            log.debug("Geocoding request URL: {}", url.replace(mapboxToken, "***"));

            MapboxGeocodingResponse response = restTemplate.getForObject(url, MapboxGeocodingResponse.class);

            if (response != null && response.getFeatures() != null && !response.getFeatures().isEmpty()) {
                MapboxFeature feature = response.getFeatures().get(0);
                double[] coordinates = feature.getGeometry().getCoordinates();

                // Mapbox возвращает [longitude, latitude]
                BigDecimal longitude = BigDecimal.valueOf(coordinates[0]).setScale(8, RoundingMode.HALF_UP);
                BigDecimal latitude = BigDecimal.valueOf(coordinates[1]).setScale(8, RoundingMode.HALF_UP);

                log.info("Successfully geocoded '{}' to [{}, {}]", address, latitude, longitude);
                return new GeoLocation(latitude, longitude);
            } else {
                throw new RuntimeException("No results found for address: " + address);
            }
        } catch (Exception e) {
            log.error("Error during Mapbox geocoding for address: {}", address, e);
            throw new RuntimeException("Mapbox geocoding service error: " + e.getMessage());
        }
    }

    /**
     * Обратное геокодирование - получение адреса по координатам
     */
    public String reverseGeocode(BigDecimal longitude, BigDecimal latitude) {
        try {
            String url = String.format("%s/%s,%s.json?access_token=%s&types=address",
                    MAPBOX_GEOCODING_URL, longitude, latitude, mapboxToken);

            MapboxGeocodingResponse response = restTemplate.getForObject(url, MapboxGeocodingResponse.class);

            if (response != null && response.getFeatures() != null && !response.getFeatures().isEmpty()) {
                return response.getFeatures().get(0).getPlace_name();
            } else {
                throw new RuntimeException("No address found for coordinates");
            }
        } catch (Exception e) {
            log.error("Error during reverse geocoding", e);
            throw new RuntimeException("Reverse geocoding error: " + e.getMessage());
        }
    }

    /**
     * Поиск ближайших мест (POI)
     */
    public List<MapboxPlace> searchNearbyPlaces(BigDecimal longitude, BigDecimal latitude, String query, int limit) {
        try {
            String url = String.format("%s/%s.json?access_token=%s&proximity=%s,%s&limit=%d&types=poi",
                    MAPBOX_GEOCODING_URL, URLEncoder.encode(query, StandardCharsets.UTF_8),
                    mapboxToken, longitude, latitude, limit);

            MapboxGeocodingResponse response = restTemplate.getForObject(url, MapboxGeocodingResponse.class);

            if (response != null && response.getFeatures() != null) {
                return response.getFeatures().stream()
                        .map(this::convertToMapboxPlace)
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error searching nearby places", e);
            return Collections.emptyList();
        }
    }

    private MapboxPlace convertToMapboxPlace(MapboxFeature feature) {
        double[] coordinates = feature.getGeometry().getCoordinates();
        return MapboxPlace.builder()
                .name(feature.getText())
                .fullName(feature.getPlace_name())
                .longitude(BigDecimal.valueOf(coordinates[0]))
                .latitude(BigDecimal.valueOf(coordinates[1]))
                .build();
    }

    private String getFormattedAddress(CreateAddressRequest request) {
        return String.format("%s, %s%s%s",
                request.getStreet(),
                request.getCity(),
                request.getRegion() != null ? ", " + request.getRegion() : "",
                request.getCountry() != null ? ", " + request.getCountry() : ""
        );
    }
}
