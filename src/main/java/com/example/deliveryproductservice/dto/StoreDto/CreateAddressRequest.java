package com.example.deliveryproductservice.dto.StoreDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAddressRequest {

    @NotBlank(message = "Street is required")
    private String street;

    @NotBlank(message = "City is required")
    private String city;

    private String region;
    private String country;
    private String postalCode;

    // Координаты (опционально - если не указаны, будет геокодирование)
    private BigDecimal latitude;
    private BigDecimal longitude;

    // Настройки геокодирования
    private Boolean autoGeocode = true; // Автоматически получать координаты
}