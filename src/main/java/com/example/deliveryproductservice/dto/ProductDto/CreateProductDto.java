package com.example.deliveryproductservice.dto.ProductDto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

@Data
public class CreateProductDto {

    @NotNull(message = "Store ID is required")
    private Long storeId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "99999999.99", message = "Price is too high")
    private BigDecimal price;

    @DecimalMin(value = "0.00", message = "Discount price cannot be negative")
    @DecimalMax(value = "99999999.99", message = "Discount price is too high")
    private BigDecimal discountPrice;

    private MultipartFile imageFile;

    private Boolean isAvailable = true;
    private Boolean isPopular = false;
}