package com.example.deliveryproductservice.service;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface StorageService {
    /**
     * Загружает файл изображения и возвращает результат загрузки
     */
    StorageResult uploadImage(MultipartFile file) throws IOException;

    /**
     * Загружает файл изображения в указанную папку
     */
    StorageResult uploadImage(MultipartFile file, String folder) throws IOException;

    /**
     * Удаляет изображение по его идентификатору
     */
    boolean deleteImage(String imageId);

    /**
     * Загружает обработанное изображение
     */
    StorageResult uploadProcessedImage(ImageConverterService.ProcessedImage processedImage) throws IOException;

    /**
     * Получает информацию об изображении
     */
    Map<String, Object> getImageInfo(String imageId);

    /**
     * Результат загрузки изображения
     */
    @Getter
    class StorageResult {
        private final String url;
        private final String imageId;

        public StorageResult(String url, String imageId) {
            this.url = url;
            this.imageId = imageId;
        }
    }
}