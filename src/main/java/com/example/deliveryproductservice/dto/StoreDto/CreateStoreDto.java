package com.example.deliveryproductservice.dto.StoreDto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import com.example.deliveryproductservice.dto.GeoLocation.CreateAddressRequest;

import java.math.BigDecimal;

@Data
public class CreateStoreDto {

    @NotBlank(message = "Store name is required")
    @Size(max = 100, message = "Store name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    // ПЛОСКИЕ ПОЛЯ АДРЕСА (вместо nested объекта)
    @NotBlank(message = "Street is required")
    private String street;

    @NotBlank(message = "City is required")
    private String city;

    private String region;
    private String country;
    private String postalCode;

    private String phone;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotNull(message = "Delivery radius is required")
    @Min(value = 1, message = "Delivery radius must be at least 1 km")
    @Max(value = 50, message = "Delivery radius cannot exceed 50 km")
    private Integer deliveryRadius;

    @NotNull(message = "Delivery fee is required")
    @DecimalMin(value = "0.00", message = "Delivery fee cannot be negative")
    @DecimalMax(value = "99999.99", message = "Delivery fee is too high")
    private BigDecimal deliveryFee;

    @NotNull(message = "Estimated delivery time is required")
    @Min(value = 10, message = "Delivery time must be at least 10 minutes")
    @Max(value = 180, message = "Delivery time cannot exceed 180 minutes")
    private Integer estimatedDeliveryTime;

    private MultipartFile imageFile;

    // Для JSON загрузки изображений
    private String imageBase64;
    private String imageName;

    private Boolean isActive = true;

    // Включение автогеокодирования
    private Boolean autoGeocode = true;

    /**
     * Создает объект CreateAddressRequest из плоских полей
     */
    public CreateAddressRequest getAddress() {
        CreateAddressRequest address = CreateAddressRequest.builder()
                .street(this.street)
                .city(this.city)
                .region(this.region)
                .country(this.country)
                .postalCode(this.postalCode)
                .autoGeocode(this.autoGeocode != null ? this.autoGeocode : true)
                .build();
        return address;
    }
}