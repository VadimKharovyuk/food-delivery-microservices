package com.example.deliveryproductservice.service;

import com.example.deliveryproductservice.dto.StoreDto.CreateStoreDto;
import com.example.deliveryproductservice.dto.StoreDto.SingleStoreResponseWrapper;
import com.example.deliveryproductservice.dto.StoreDto.StoreResponseDto;
import com.example.deliveryproductservice.dto.StoreDto.StoreResponseWrapper;


public interface StoreService {
    StoreResponseDto createStore(CreateStoreDto createStoreDto, Long ownerId);
     StoreResponseWrapper getActiveStores(int page, int size);
     StoreResponseWrapper getStoresByOwner(Long ownerId, int page, int size) ;
    SingleStoreResponseWrapper getStoreById(Long storeId);
}
