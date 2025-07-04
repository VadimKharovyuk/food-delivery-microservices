package com.example.deliveryproductservice.service;

import com.example.deliveryproductservice.dto.StoreDto.*;
import org.springframework.web.multipart.MultipartFile;



public interface StoreService {
    StoreResponseDto createStore(CreateStoreDto createStoreDto, Long ownerId);

    StoreResponseWrapper getActiveStores(int page, int size);

    StoreResponseWrapper getStoresByOwner(Long ownerId, int page, int size);

    SingleStoreResponseWrapper getStoreById(Long storeId);

    StoreUIResponseWrapper getActiveStoresForUI();

    StoreBriefResponseWrapper getActiveStoresBrief(int page, int size);


}
