package com.example.deliveryproductservice.mapper;

import com.example.deliveryproductservice.dto.StoreDto.StoreResponseDto;
import com.example.deliveryproductservice.dto.StoreDto.StoreUIDto;
import com.example.deliveryproductservice.dto.StoreDto.StoreUIProjection;
import com.example.deliveryproductservice.model.Store;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class StoreMapper {
    /**
     * 📤 Store Entity → StoreResponseDto
     */
    public StoreResponseDto mapToResponseDto(Store store) {
        if (store == null) {
            return null;
        }

        StoreResponseDto dto = new StoreResponseDto();
        dto.setId(store.getId());
        dto.setOwnerId(store.getOwnerId());
        dto.setName(store.getName());
        dto.setDescription(store.getDescription());
        dto.setAddress(store.getAddress());
        dto.setPhone(store.getPhone());
        dto.setEmail(store.getEmail());
        dto.setIsActive(store.getIsActive());
        dto.setRating(store.getRating());
        dto.setDeliveryRadius(store.getDeliveryRadius());
        dto.setDeliveryFee(store.getDeliveryFee());
        dto.setEstimatedDeliveryTime(store.getEstimatedDeliveryTime());
        dto.setPicUrl(store.getPicUrl());
        dto.setCreatedAt(store.getCreatedAt());
        dto.setUpdatedAt(store.getUpdatedAt());

        log.debug("Mapped Store {} to ResponseDto", store.getId());
        return dto;
    }

    public StoreUIDto mapToUIDto(Store store) {
        if (store == null) {
            return null;
        }

        return StoreUIDto.builder()
                .name(store.getName())
                .picUrl(store.getPicUrl())
                .rating(store.getRating() != null ? store.getRating() : BigDecimal.valueOf(0.0))
                .estimatedDeliveryTime(store.getEstimatedDeliveryTime())
                .build();
    }

    public StoreUIDto mapProjectionToUIDto(StoreUIProjection projection) {
        return StoreUIDto.builder()
                .name(projection.getName())
                .picUrl(projection.getPicUrl())
                .rating(projection.getRating() != null ? projection.getRating() : BigDecimal.ZERO)
                .estimatedDeliveryTime(projection.getEstimatedDeliveryTime())
                .build();
    }
}
