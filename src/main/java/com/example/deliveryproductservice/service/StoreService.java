package com.example.deliveryproductservice.service;

import com.example.deliveryproductservice.dto.StoreDto.CreateStoreDto;
import com.example.deliveryproductservice.dto.StoreDto.StoreResponseDto;


public interface StoreService {
    StoreResponseDto createStore(CreateStoreDto createStoreDto, Long ownerId);
}
