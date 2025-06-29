package com.example.deliveryproductservice.dto.StoreDto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreBriefResponseWrapper {
    private List<StoreBriefDto> stores;
    private Integer totalCount;
    private Boolean hasNext;
    private Boolean hasPrevious;
    private Integer currentPage;
    private Integer pageSize;
    private Boolean success;
    private String message;
    private LocalDateTime timestamp;

    // Для успешного ответа с пагинацией
    public static StoreBriefResponseWrapper success(Slice<StoreBriefDto> slice) {
        return StoreBriefResponseWrapper.builder()
                .stores(slice.getContent())
                .totalCount(slice.getContent().size())
                .hasNext(slice.hasNext())
                .hasPrevious(slice.hasPrevious())
                .currentPage(slice.getNumber())
                .pageSize(slice.getSize())
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Для ошибок
    public static StoreBriefResponseWrapper error(String message) {
        return StoreBriefResponseWrapper.builder()
                .stores(Collections.emptyList())
                .totalCount(0)
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
