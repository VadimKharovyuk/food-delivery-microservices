package com.example.deliveryproductservice.service;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {
    /**
     * Загружает файл изображения и возвращает результат загрузки
     */
    StorageResult uploadImage(MultipartFile file) throws IOException;

    /**
     * Удаляет изображение по его идентификатору
     */
    boolean deleteImage(String imageId);

    // ✅ НОВЫЙ метод для обработанных изображений
    StorageResult uploadProcessedImage(ImageConverterService.ProcessedImage processedImage) throws IOException;


    /**
     * Получает информацию об изображении
     */
    Object getImageInfo(String imageId);

    /**
     * Результат загрузки изображения
     */
    @Getter
    class StorageResult {
        private String url;
        private String imageId;

        public StorageResult(String url, String imageId) {
            this.url = url;
            this.imageId = imageId;
        }

    }
}
