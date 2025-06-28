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
public class CategoryBriefResponseWrapper {
    private List<CategoryBaseProjection> categories;
    private Integer totalCount;
    private Boolean success;
    private String message;
    private LocalDateTime timestamp;

    public static CategoryBriefResponseWrapper success(List<CategoryBaseProjection> categories) {
        return CategoryBriefResponseWrapper.builder()
                .categories(categories)
                .totalCount(categories.size())
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
