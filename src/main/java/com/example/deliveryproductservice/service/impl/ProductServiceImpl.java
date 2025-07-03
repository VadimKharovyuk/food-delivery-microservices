package com.example.deliveryproductservice.service.impl;


import com.example.deliveryproductservice.dto.ProductDto.*;
import com.example.deliveryproductservice.mapper.ProductMapper;
import com.example.deliveryproductservice.model.Product;
import com.example.deliveryproductservice.repository.ProductRepository;
import com.example.deliveryproductservice.service.ImageConverterService;
import com.example.deliveryproductservice.service.ProductService;
import com.example.deliveryproductservice.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final StorageService storageService;
    private final ImageConverterService imageConverterService;


    /**
     * Создает новый продукт с изображением
     */
    public SingleProductResponseWrapper createProduct(CreateProductDto createProductDto,
                                                      MultipartFile imageFile,
                                                      Long userId) {
        try {
            // Валидация изображения
            if (imageFile == null || imageFile.isEmpty()) {
                return SingleProductResponseWrapper.builder()
                        .success(false)
                        .message("Product image is required")
                        .timestamp(java.time.LocalDateTime.now())
                        .build();
            }

            // Проверка типа файла
            if (!isValidImageFile(imageFile)) {
                return SingleProductResponseWrapper.builder()
                        .success(false)
                        .message("Invalid image format. Only JPG, PNG, GIF, WEBP are allowed")
                        .timestamp(java.time.LocalDateTime.now())
                        .build();
            }

            // Обработка и загрузка изображения
            StorageService.StorageResult uploadResult = processAndUploadImage(imageFile);

            // Создание продукта
            Product product = Product.builder()
                    .storeId(createProductDto.getStoreId())
                    .categoryId(createProductDto.getCategoryId())
                    .name(createProductDto.getName())
                    .description(createProductDto.getDescription())
                    .price(createProductDto.getPrice())
                    .discountPrice(createProductDto.getDiscountPrice())
                    .picUrl(uploadResult.getUrl())
                    .picId(uploadResult.getImageId())
                    .isPopular(createProductDto.getIsPopular())
                    .isAvailable(createProductDto.getIsAvailable())
                    .rating(BigDecimal.ZERO)
                    .build();

            Product savedProduct = productRepository.save(product);

            log.info("Product created successfully with ID: {} by user: {}",
                    savedProduct.getId(), userId);

            return SingleProductResponseWrapper.success(convertToResponseDto(savedProduct));

        } catch (IOException e) {
            log.error("Error uploading image for product creation", e);
            return SingleProductResponseWrapper.builder()
                    .success(false)
                    .message("Failed to upload product image: " + e.getMessage())
                    .timestamp(java.time.LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Error creating product", e);
            return SingleProductResponseWrapper.builder()
                    .success(false)
                    .message("Failed to create product: " + e.getMessage())
                    .timestamp(java.time.LocalDateTime.now())
                    .build();
        }
    }

    /**
     * Обрабатывает и загружает изображение - ИСПРАВЛЕНО
     */
    private StorageService.StorageResult processAndUploadImage(MultipartFile imageFile) throws IOException {
        // ИСПРАВЛЕНО: Обработка изображения через ImageConverterService
        ImageConverterService.ProcessedImage processedImage =
                imageConverterService.processProductImage(imageFile);

        // Загрузка обработанного изображения через StorageService
        return storageService.uploadProcessedImage(processedImage);
    }

    /**
     * Альтернативный метод - простая загрузка без обработки
     */
    private StorageService.StorageResult uploadImageDirect(MultipartFile imageFile) throws IOException {
        // Если не нужна обработка изображения, можно использовать прямую загрузку
        return storageService.uploadImage(imageFile, "products");
    }

    /**
     * Конвертирует Product в ProductResponseDto
     */
    private ProductResponseDto convertToResponseDto(Product product) {
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
        dto.setRating(product.getRating());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        dto.setIsPopular(product.getIsPopular());

        return dto;
    }

    /**
     * Проверяет, является ли файл допустимым изображением
     */
    private boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif") ||
                        contentType.equals("image/webp")
        );
    }

//
//    @Override
//    @Transactional
//    public SingleProductResponseWrapper createProduct(CreateProductDto createProductDto, Long userId) {
//        log.info("Creating new product: {} for store: {} by user: {}",
//                createProductDto.getName(), createProductDto.getStoreId(), userId);
//
//        try {
//            // 1. Создаем сущность Product
//            Product product = productMapper.mapFromCreateDto(createProductDto);
//
//            // 2. Обрабатываем изображение
//            handleProductImage(product, createProductDto);
//
//            // 3. Сохраняем в базу
//            Product savedProduct = productRepository.save(product);
//
//            log.info("✅ Product created successfully with ID: {} by user: {}",
//                    savedProduct.getId(), userId);
//
//            // 4. Возвращаем DTO
//            ProductResponseDto responseDto = productMapper.mapToResponseDto(savedProduct);
//            return SingleProductResponseWrapper.success(responseDto);
//
//        } catch (Exception e) {
//            log.error("❌ Error creating product for user {}: {}", userId, e.getMessage(), e);
//            throw new RuntimeException("Failed to create product: " + e.getMessage(), e);
//        }
//    }

    @Override
    @Transactional(readOnly = true)
    public SingleProductResponseWrapper getProductById(Long productId) {
        log.debug("Getting product by ID: {}", productId);

        Optional<Product> productOptional = productRepository.findByIdAndIsAvailableTrue(productId);

        if (productOptional.isPresent()) {
            ProductResponseDto responseDto = productMapper.mapToResponseDto(productOptional.get());
            return SingleProductResponseWrapper.success(responseDto);
        } else {
            return SingleProductResponseWrapper.notFound(productId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseWrapper getAllAvailableProducts(int page, int size) {
        log.debug("Getting all available products with pagination: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Slice<Product> productSlice = productRepository.findByIsAvailableTrueOrderByCreatedAtDesc(pageable);

        Slice<ProductResponseDto> productDtoSlice = productSlice.map(productMapper::mapToResponseDto);

        return ProductResponseWrapper.success(productDtoSlice);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseWrapper getProductsByStore(Long storeId, int page, int size) {
        log.debug("Getting products for store {} with pagination: page={}, size={}", storeId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Slice<Product> productSlice = productRepository.findByStoreIdAndIsAvailableTrueOrderByCreatedAtDesc(storeId, pageable);

        Slice<ProductResponseDto> productDtoSlice = productSlice.map(productMapper::mapToResponseDto);

        return ProductResponseWrapper.success(productDtoSlice);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseWrapper getProductsByCategory(Long categoryId, int page, int size) {
        log.debug("Getting products for category {} with pagination: page={}, size={}", categoryId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Slice<Product> productSlice = productRepository.findByCategoryIdAndIsAvailableTrueOrderByCreatedAtDesc(categoryId, pageable);

        Slice<ProductResponseDto> productDtoSlice = productSlice.map(productMapper::mapToResponseDto);

        return ProductResponseWrapper.success(productDtoSlice);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseWrapper searchProductsByName(String name, int page, int size) {
        log.debug("Searching products by name '{}' with pagination: page={}, size={}", name, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Slice<Product> productSlice = productRepository.findByNameContainingIgnoreCaseAndIsAvailableTrueOrderByCreatedAtDesc(name, pageable);

        Slice<ProductResponseDto> productDtoSlice = productSlice.map(productMapper::mapToResponseDto);

        return ProductResponseWrapper.success(productDtoSlice);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductBriefResponseWrapper getProductsBriefByStore(Long storeId, int page, int size) {
        log.debug("Getting brief products for store {} with pagination: page={}, size={}", storeId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Slice<ProductBriefProjection> productSlice = productRepository.findProductsBriefByStore(storeId, pageable);

        return ProductBriefResponseWrapper.success(productSlice);
    }

    @Override
    @Transactional
    public SingleProductResponseWrapper updateProduct(Long productId, CreateProductDto updateProductDto, Long userId) {
        log.info("Updating product {} by user: {}", productId, userId);

        try {
            Optional<Product> productOptional = productRepository.findById(productId);

            if (productOptional.isEmpty()) {
                return SingleProductResponseWrapper.notFound(productId);
            }

            Product product = productOptional.get();
            String oldImageId = product.getPicId(); // Сохраняем старый ID для удаления

            // Обновляем поля
            productMapper.updateProductFromDto(product, updateProductDto);

            // Обновляем изображение если есть новое
            if (updateProductDto.getImageFile() != null && !updateProductDto.getImageFile().isEmpty()) {
                handleProductImage(product, updateProductDto);

                // Удаляем старое изображение
                if (oldImageId != null && !oldImageId.startsWith("default_")) {
                    try {
                        storageService.deleteImage(oldImageId);
                        log.debug("🗑️ Old image deleted: {}", oldImageId);
                    } catch (Exception e) {
                        log.warn("⚠️ Failed to delete old image {}: {}", oldImageId, e.getMessage());
                    }
                }
            }

            Product savedProduct = productRepository.save(product);

            log.info("✅ Product {} updated successfully by user: {}", productId, userId);

            ProductResponseDto responseDto = productMapper.mapToResponseDto(savedProduct);
            return SingleProductResponseWrapper.success(responseDto);

        } catch (Exception e) {
            log.error("❌ Error updating product {}: {}", productId, e.getMessage(), e);
            throw new RuntimeException("Failed to update product: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId, Long userId) {
        log.info("Deleting product {} by user: {}", productId, userId);

        Optional<Product> productOptional = productRepository.findById(productId);

        if (productOptional.isEmpty()) {
            throw new RuntimeException("Product not found with ID: " + productId);
        }

        Product product = productOptional.get();
        product.setIsAvailable(false); // Мягкое удаление

        productRepository.save(product);

        log.info("✅ Product {} soft deleted successfully by user: {}", productId, userId);

        // Примечание: изображение не удаляем при мягком удалении,
        // так как продукт может быть восстановлен
    }


    /**
     * Обработка изображения продукта с использованием StorageService
     */
    private void handleProductImage(Product product, CreateProductDto createProductDto) {
        if (createProductDto.getImageFile() != null && !createProductDto.getImageFile().isEmpty()) {
            try {
                log.debug("📸 Uploading product image...");

                StorageService.StorageResult result = storageService.uploadImage(createProductDto.getImageFile());

                product.setPicId(result.getImageId());
                product.setPicUrl(result.getUrl());

                log.info("✅ Product image uploaded successfully: {} -> {}",
                        result.getImageId(), result.getUrl());

            } catch (IOException e) {
                log.error("❌ Failed to upload product image: {}", e.getMessage(), e);

                // Используем дефолтное изображение при ошибке загрузки
                setDefaultProductImage(product);

                // Можно также выбросить исключение, если загрузка изображения критична
                // throw new RuntimeException("Failed to upload product image: " + e.getMessage(), e);
            }
        } else {
            // Дефолтное изображение, если файл не предоставлен
            setDefaultProductImage(product);
        }
    }

    /**
     * Устанавливает дефолтное изображение для продукта
     */
    private void setDefaultProductImage(Product product) {
        product.setPicId("default_product_" + System.currentTimeMillis());
        product.setPicUrl("https://via.placeholder.com/400x400/f0f0f0/999999?text=No+Image");

        log.debug("🖼️ Set default image for product");
    }

    /**
     * Жесткое удаление продукта (с удалением изображения)
     * Метод для админских операций
     */
    @Transactional
    @Override
    public void hardDeleteProduct(Long productId, Long userId) {
        log.info("Hard deleting product {} by user: {}", productId, userId);

        Optional<Product> productOptional = productRepository.findById(productId);

        if (productOptional.isEmpty()) {
            throw new RuntimeException("Product not found with ID: " + productId);
        }

        Product product = productOptional.get();
        String imageId = product.getPicId();

        // Удаляем из базы данных
        productRepository.delete(product);

        // Удаляем изображение
        if (imageId != null && !imageId.startsWith("default_")) {
            try {
                boolean deleted = storageService.deleteImage(imageId);
                if (deleted) {
                    log.info("🗑️ Product image deleted: {}", imageId);
                } else {
                    log.warn("⚠️ Failed to delete product image: {}", imageId);
                }
            } catch (Exception e) {
                log.error("❌ Error deleting product image {}: {}", imageId, e.getMessage(), e);
            }
        }

        log.info("✅ Product {} hard deleted successfully by user: {}", productId, userId);
    }

}