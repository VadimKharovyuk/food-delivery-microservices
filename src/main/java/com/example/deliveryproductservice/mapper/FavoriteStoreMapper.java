package com.example.deliveryproductservice.mapper;

import com.example.deliveryproductservice.dto.FavoriteStore.FavoriteStoreResponseDto;
import com.example.deliveryproductservice.model.FavoriteStore;
import com.example.deliveryproductservice.model.Store;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class FavoriteStoreMapper {

    public FavoriteStoreResponseDto toResponseDto(FavoriteStore favoriteStore) {
        if (favoriteStore == null) {
            return null;
        }

        return FavoriteStoreResponseDto.builder()
                .id(favoriteStore.getId())
                .userId(favoriteStore.getUserId())
                .store(mapStoreToStoreInfo(favoriteStore.getStore()))
                .createdAt(favoriteStore.getCreatedAt())
                .build();
    }


    public List<FavoriteStoreResponseDto> toResponseDtoList(List<FavoriteStore> favoriteStores) {
        if (favoriteStores == null || favoriteStores.isEmpty()) {
            return List.of();
        }

        return favoriteStores.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }


    private FavoriteStoreResponseDto.StoreInfo mapStoreToStoreInfo(Store store) {
        if (store == null) {
            return null;
        }

        return FavoriteStoreResponseDto.StoreInfo.builder()
                .id(store.getId())
                .name(store.getName())
                .description(store.getDescription())
                .picUrl(store.getPicUrl())
                .isActive(store.getIsActive())
                .rating(store.getRating())
                .deliveryRadius(store.getDeliveryRadius())
                .estimatedDeliveryTime(store.getEstimatedDeliveryTime())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }


    public FavoriteStore createFavoriteStore(Long userId, Store store) {
        return FavoriteStore.builder()
                .userId(userId)
                .store(store)
                .build();
    }
}