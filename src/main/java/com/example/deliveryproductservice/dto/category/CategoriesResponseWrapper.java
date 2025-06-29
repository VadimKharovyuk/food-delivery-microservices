package com.example.deliveryproductservice.dto.category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriesResponseWrapper {
    private List<CategoryResponseDto> categories;
    private Integer totalCount;
    private Boolean success;
    private String message;
    private LocalDateTime timestamp;

    // Конструктор для успешного ответа
    public static CategoriesResponseWrapper success(List<CategoryResponseDto> categories) {
        return CategoriesResponseWrapper.builder()
                .categories(categories)
                .totalCount(categories.size())
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }
}