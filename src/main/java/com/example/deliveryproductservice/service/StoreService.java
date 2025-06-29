package com.example.deliveryproductservice.service;

import com.example.deliveryproductservice.dto.StoreDto.*;

import java.util.List;


public interface StoreService {
    StoreResponseDto createStore(CreateStoreDto createStoreDto, Long ownerId);

    StoreResponseWrapper getActiveStores(int page, int size);

    StoreResponseWrapper getStoresByOwner(Long ownerId, int page, int size);

    SingleStoreResponseWrapper getStoreById(Long storeId);

    StoreUIResponseWrapper getActiveStoresForUI();



}
