package com.example.deliveryproductservice.dto.category;

import com.example.deliveryproductservice.repository.CategoryRepository;
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
public class CategoryStatsResponseWrapper {
    private List<CategoryRepository.CategoryStatsProjection> statistics;
    private Integer totalCategories;
    private Boolean success;
    private String message;
    private LocalDateTime generatedAt;

    public static CategoryStatsResponseWrapper success(List<CategoryRepository.CategoryStatsProjection> stats) {
        return CategoryStatsResponseWrapper.builder()
                .statistics(stats)
                .totalCategories(stats.size())
                .success(true)
                .generatedAt(LocalDateTime.now())
                .build();
    }
}