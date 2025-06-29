package com.example.deliveryproductservice.dto.ProductDto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

// Для списка продуктов с пагинацией
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseWrapper {
    private List<ProductResponseDto> products;
    private Integer totalCount;
    private Boolean hasNext;
    private Boolean hasPrevious;
    private Integer currentPage;
    private Integer pageSize;
    private Boolean success;
    private String message;
    private LocalDateTime timestamp;

    public static ProductResponseWrapper success(Slice<ProductResponseDto> slice) {
        return ProductResponseWrapper.builder()
                .products(slice.getContent())
                .totalCount(slice.getContent().size())
                .hasNext(slice.hasNext())
                .hasPrevious(slice.hasPrevious())
                .currentPage(slice.getNumber())
                .pageSize(slice.getSize())
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ProductResponseWrapper error(String message) {
        return ProductResponseWrapper.builder()
                .products(Collections.emptyList())
                .totalCount(0)
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
