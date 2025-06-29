package com.example.deliveryproductservice.dto.StoreDto;

import com.example.deliveryproductservice.dto.StoreDto.StoreUIDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreUIResponseWrapper {
    private List<StoreUIDto> stores;
    private Integer totalCount;
    private Boolean success;
    private String message;
    private LocalDateTime timestamp;

    // Для успешного ответа
    public static StoreUIResponseWrapper success(List<StoreUIDto> stores) {
        return StoreUIResponseWrapper.builder()
                .stores(stores)
                .totalCount(stores.size())
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Для ошибок
    public static StoreUIResponseWrapper error(String message) {
        return StoreUIResponseWrapper.builder()
                .stores(Collections.emptyList())
                .totalCount(0)
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}