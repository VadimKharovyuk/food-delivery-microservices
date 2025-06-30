package com.example.deliveryproductservice.service;

import com.example.deliveryproductservice.dto.GeoLocation.*;
import com.example.deliveryproductservice.model.Address;
import jakarta.annotation.PostConstruct;
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

    @Value("${mapbox.access.token:}")
    private String mapboxToken;

    private final RestTemplate restTemplate;
    private static final String MAPBOX_GEOCODING_URL = "https://api.mapbox.com/geocoding/v5/mapbox.places";

    // Флаг для определения доступности геокодирования
    private boolean geocodingAvailable = false;

    public MapboxGeocodingService() {
        this.restTemplate = new RestTemplate();
    }

    @PostConstruct
    public void initializeMapboxService() {
        log.info("🗺️ Initializing Mapbox Geocoding Service...");

        // Проверяем токен
        if (mapboxToken == null || mapboxToken.trim().isEmpty()) {
            log.warn("⚠️ Mapbox access token is not configured!");
            log.warn("⚠️ Add 'mapbox.access.token=your_token_here' to application.properties");
            log.warn("⚠️ Get your token at: https://account.mapbox.com/access-tokens/");
            log.warn("⚠️ Geocoding will use fallback coordinates");
            geocodingAvailable = false;
        } else {
            // Маскируем токен для безопасности в логах
            String maskedToken = maskToken(mapboxToken);
            log.info("🔑 Mapbox token loaded: {}", maskedToken);

            // Проверяем валидность токена
            try {
                validateToken();
                geocodingAvailable = true;
                log.info("✅ Mapbox Geocoding Service initialized successfully");
                log.info("🌍 Geocoding is ENABLED");
            } catch (Exception e) {
                log.error("❌ Mapbox token validation failed: {}", e.getMessage());
                log.warn("⚠️ Geocoding will use fallback coordinates");
                geocodingAvailable = false;
            }
        }

        // Выводим статус сервиса
        log.info("📊 Mapbox Service Status:");
        log.info("   • Token configured: {}", mapboxToken != null && !mapboxToken.trim().isEmpty());
        log.info("   • Geocoding available: {}", geocodingAvailable);
        log.info("   • Fallback mode: {}", !geocodingAvailable);
    }

    /**
     * Проверяет валидность токена делая тестовый запрос
     */
    private void validateToken() {
        try {
            log.debug("🔍 Validating Mapbox token...");

            // Делаем тестовый запрос с простым адресом
            String testAddress = "New York";
            String encodedAddress = URLEncoder.encode(testAddress, StandardCharsets.UTF_8);
            String url = String.format("%s/%s.json?access_token=%s&limit=1",
                    MAPBOX_GEOCODING_URL, encodedAddress, mapboxToken);

            MapboxGeocodingResponse response = restTemplate.getForObject(url, MapboxGeocodingResponse.class);

            if (response != null && response.getFeatures() != null) {
                log.info("✅ Mapbox token is valid - test geocoding successful");
            } else {
                throw new RuntimeException("Invalid response from Mapbox API");
            }

        } catch (Exception e) {
            throw new RuntimeException("Token validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Маскирует токен для безопасного логирования
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "***invalid***";
        }

        return token.substring(0, 8) + "..." + token.substring(token.length() - 4)
                + " (length: " + token.length() + ")";
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
            log.info("📍 Using provided coordinates: {}, {}", request.getLatitude(), request.getLongitude());
        }
        // Пробуем геокодирование если доступно
        else if (geocodingAvailable && (request.getAutoGeocode() == null || request.getAutoGeocode())) {
            try {
                String address = getFormattedAddress(request);
                log.info("🌍 Geocoding address: {}", address);

                GeoLocation coordinates = geocodeAddress(address);
                builder.latitude(coordinates.getLatitude())
                        .longitude(coordinates.getLongitude())
                        .fullAddress(address);
                log.info("✅ Geocoded '{}' → [{}, {}]",
                        address, coordinates.getLatitude(), coordinates.getLongitude());

            } catch (Exception e) {
                log.warn("⚠️ Geocoding failed: {}, using fallback", e.getMessage());
                return createAddressWithFallbackCoordinates(builder, request);
            }
        } else {
            // Используем fallback координаты
            log.info("🔄 Using fallback coordinates (geocoding unavailable or disabled)");
            return createAddressWithFallbackCoordinates(builder, request);
        }

        return builder.build();
    }

    /**
     * Создает адрес с fallback координатами
     */
    private Address createAddressWithFallbackCoordinates(Address.AddressBuilder builder, CreateAddressRequest request) {
        BigDecimal[] fallbackCoords = getFallbackCoordinatesByCity(request.getCity(), request.getCountry());

        builder.latitude(fallbackCoords[0])
                .longitude(fallbackCoords[1])
                .fullAddress(getFormattedAddress(request));

        log.info("📍 Fallback coordinates for {}: [{}, {}]",
                request.getCity(), fallbackCoords[0], fallbackCoords[1]);

        return builder.build();
    }

    /**
     * Получает примерные координаты по названию города
     */
    private BigDecimal[] getFallbackCoordinatesByCity(String city, String country) {
        if (city == null) city = "";
        if (country == null) country = "";

        String key = (city + ", " + country).toLowerCase();

        // Украина
        if (key.contains("харьков") || key.contains("kharkiv") || key.contains("kharkov")) {
            return new BigDecimal[]{new BigDecimal("49.9935"), new BigDecimal("36.2304")};
        }
        if (key.contains("киев") || key.contains("kiev") || key.contains("kyiv")) {
            return new BigDecimal[]{new BigDecimal("50.4501"), new BigDecimal("30.5234")};
        }
        if (key.contains("одесса") || key.contains("odesa") || key.contains("odessa")) {
            return new BigDecimal[]{new BigDecimal("46.4825"), new BigDecimal("30.7233")};
        }

        // Россия
        if (key.contains("москва") || key.contains("moscow")) {
            return new BigDecimal[]{new BigDecimal("55.7558"), new BigDecimal("37.6176")};
        }
        if (key.contains("петербург") || key.contains("spb") || key.contains("petersburg")) {
            return new BigDecimal[]{new BigDecimal("59.9311"), new BigDecimal("30.3609")};
        }

        // США
        if (key.contains("new york")) {
            return new BigDecimal[]{new BigDecimal("40.7128"), new BigDecimal("-74.0060")};
        }
        if (key.contains("los angeles")) {
            return new BigDecimal[]{new BigDecimal("34.0522"), new BigDecimal("-118.2437")};
        }

        // Германия
        if (key.contains("berlin")) {
            return new BigDecimal[]{new BigDecimal("52.5200"), new BigDecimal("13.4050")};
        }

        // По умолчанию - центр Европы
        log.debug("🌍 Using default European coordinates for unknown city: {}", city);
        return new BigDecimal[]{new BigDecimal("50.0000"), new BigDecimal("20.0000")};
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
