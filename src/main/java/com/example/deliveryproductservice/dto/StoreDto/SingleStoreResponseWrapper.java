package com.example.deliveryproductservice.dto.StoreDto;

import com.example.deliveryproductservice.dto.StoreDto.StoreResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingleStoreResponseWrapper {
    private StoreResponseDto store;
    private Boolean success;
    private String message;
    private LocalDateTime timestamp;

    public static SingleStoreResponseWrapper success(StoreResponseDto store) {
        return SingleStoreResponseWrapper.builder()
                .store(store)
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static SingleStoreResponseWrapper notFound(Long id) {
        return SingleStoreResponseWrapper.builder()
                .store(null)
                .success(false)
                .message("Store not found with ID: " + id)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static SingleStoreResponseWrapper error(String message) {
        return SingleStoreResponseWrapper.builder()
                .store(null)
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}