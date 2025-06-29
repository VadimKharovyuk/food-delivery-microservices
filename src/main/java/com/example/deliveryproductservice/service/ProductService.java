package com.example.deliveryproductservice.service;


import com.example.deliveryproductservice.dto.ProductDto.CreateProductDto;
import com.example.deliveryproductservice.dto.ProductDto.ProductBriefResponseWrapper;
import com.example.deliveryproductservice.dto.ProductDto.ProductResponseWrapper;
import com.example.deliveryproductservice.dto.ProductDto.SingleProductResponseWrapper;
import org.springframework.data.domain.Slice;

public interface ProductService {

    // 🛍️ Создание продукта
    SingleProductResponseWrapper createProduct(CreateProductDto createProductDto, Long userId);

    // 🔍 Получение одного продукта
    SingleProductResponseWrapper getProductById(Long productId);

    // 📋 Получение всех доступных продуктов (с пагинацией)
    ProductResponseWrapper getAllAvailableProducts(int page, int size);

    // 🏪 Получение продуктов конкретного магазина
    ProductResponseWrapper getProductsByStore(Long storeId, int page, int size);

    // 📂 Получение продуктов конкретной категории
    ProductResponseWrapper getProductsByCategory(Long categoryId, int page, int size);

    // 🔎 Поиск продуктов по названию
    ProductResponseWrapper searchProductsByName(String name, int page, int size);

    // 📊 Краткая информация о продуктах магазина (для быстрой загрузки)
    ProductBriefResponseWrapper getProductsBriefByStore(Long storeId, int page, int size);

    // ✏️ Обновление продукта
    SingleProductResponseWrapper updateProduct(Long productId, CreateProductDto updateProductDto, Long userId);

    // 🗑️ Удаление продукта (мягкое удаление)
    void deleteProduct(Long productId, Long userId);
     void hardDeleteProduct(Long productId, Long userId);
}