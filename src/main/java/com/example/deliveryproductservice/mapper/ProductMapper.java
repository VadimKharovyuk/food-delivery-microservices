package com.example.deliveryproductservice.mapper;

import com.example.deliveryproductservice.dto.ProductDto.CreateProductDto;

import com.example.deliveryproductservice.dto.ProductDto.ProductResponseDto;
import com.example.deliveryproductservice.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

        public ProductResponseDto mapToResponseDto(Product product) {
            if (product == null) {
                return null;
            }

            ProductResponseDto dto = new ProductResponseDto();
            dto.setId(product.getId());
            dto.setStoreId(product.getStoreId());
            dto.setCategoryId(product.getCategoryId());
            dto.setName(product.getName());
            dto.setDescription(product.getDescription());
            dto.setPrice(product.getPrice());
            dto.setDiscountPrice(product.getDiscountPrice());
            dto.setPicUrl(product.getPicUrl());
            dto.setIsAvailable(product.getIsAvailable());
            dto.setIsPopular(product.getIsPopular());
            dto.setRating(product.getRating());
            dto.setCreatedAt(product.getCreatedAt());
            dto.setUpdatedAt(product.getUpdatedAt());

            return dto;
        }

        public Product mapFromCreateDto(CreateProductDto createDto) {
            if (createDto == null) {
                return null;
            }

            return Product.builder()
                    .storeId(createDto.getStoreId())
                    .categoryId(createDto.getCategoryId())
                    .name(createDto.getName())
                    .description(createDto.getDescription())
                    .price(createDto.getPrice())
                    .discountPrice(createDto.getDiscountPrice())
                    .isAvailable(createDto.getIsAvailable())
                    .isPopular(createDto.getIsPopular())
                    .build();
        }

        public void updateProductFromDto(Product product, CreateProductDto updateDto) {
            if (product == null || updateDto == null) {
                return;
            }

            product.setName(updateDto.getName());
            product.setDescription(updateDto.getDescription());
            product.setPrice(updateDto.getPrice());
            product.setDiscountPrice(updateDto.getDiscountPrice());
            product.setIsAvailable(updateDto.getIsAvailable());
            product.setIsPopular(updateDto.getIsPopular());
        }
    }
