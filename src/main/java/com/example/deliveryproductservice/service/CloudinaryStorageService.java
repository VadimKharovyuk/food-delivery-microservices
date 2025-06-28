
package com.example.deliveryproductservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryStorageService implements StorageService {

    private final Cloudinary cloudinary;

    @Override
    public StorageResult uploadImage(MultipartFile file) throws IOException {
        return uploadImage(file, "stores");
    }

    public StorageResult uploadImage(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл отсутствует или пуст");
        }

        try {
            // Генерируем уникальный public_id
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String randomSuffix = String.valueOf(System.nanoTime()).substring(0, 6);
            String publicId = folder + "/" + timestamp + "_" + randomSuffix;

            Map<String, Object> params = ObjectUtils.asMap(
                    "resource_type", "image",
                    "public_id", publicId,
                    "folder", folder,
                    "overwrite", false,
                    "quality", "auto:good",
                    "format", "auto",
                    "tags", "store_image"
            );

            log.info("🚀 Загружаем изображение в Cloudinary: {}", file.getOriginalFilename());

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

            String url = (String) uploadResult.get("secure_url");
            String resultPublicId = (String) uploadResult.get("public_id");

            log.info("✅ Изображение успешно загружено. URL: {}, Public ID: {}", url, resultPublicId);

            return new StorageResult(url, resultPublicId);
        } catch (Exception e) {
            log.error("❌ Ошибка при загрузке изображения: {}", e.getMessage(), e);
            throw new IOException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public StorageResult uploadProcessedImage(ImageConverterService.ProcessedImage processedImage) throws IOException {
        return uploadProcessedImage(processedImage, "stores");
    }

    public StorageResult uploadProcessedImage(ImageConverterService.ProcessedImage processedImage, String folder) throws IOException {
        MultipartFile convertedFile = new ProcessedImageMultipartFile(processedImage);
        return uploadImage(convertedFile, folder);
    }

    @Override
    public boolean deleteImage(String publicId) {
        if (publicId == null || publicId.isEmpty()) {
            log.warn("⚠️ Попытка удаления изображения с пустым publicId");
            return false;
        }

        try {
            log.info("🗑️ Удаляем изображение из Cloudinary. Public ID: {}", publicId);

            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            boolean success = "ok".equals(result.get("result"));

            if (success) {
                log.info("✅ Изображение успешно удалено. Public ID: {}", publicId);
            } else {
                log.warn("⚠️ Не удалось удалить изображение. Public ID: {}, Результат: {}", publicId, result);
            }

            return success;
        } catch (Exception e) {
            log.error("❌ Ошибка при удалении изображения. Public ID: {}", publicId, e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getImageInfo(String publicId) {
        if (publicId == null || publicId.isEmpty()) {
            throw new IllegalArgumentException("Public ID не может быть пустым");
        }

        try {
            log.info("ℹ️ Получаем информацию об изображении. Public ID: {}", publicId);
            return cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.error("❌ Ошибка при получении информации об изображении. Public ID: {}", publicId, e);
            return Collections.emptyMap();
        }
    }

    // Wrapper класс для ProcessedImage
    private static class ProcessedImageMultipartFile implements MultipartFile {
        private final ImageConverterService.ProcessedImage processedImage;

        public ProcessedImageMultipartFile(ImageConverterService.ProcessedImage processedImage) {
            this.processedImage = processedImage;
        }

        @Override
        public String getName() { return "store_image"; }

        @Override
        public String getOriginalFilename() { return processedImage.getFileName(); }

        @Override
        public String getContentType() { return processedImage.getContentType(); }

        @Override
        public boolean isEmpty() {
            return processedImage.getBytes() == null || processedImage.getBytes().length == 0;
        }

        @Override
        public long getSize() { return processedImage.getBytes().length; }

        @Override
        public byte[] getBytes() { return processedImage.getBytes(); }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(processedImage.getBytes());
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            try (FileOutputStream fos = new FileOutputStream(dest)) {
                fos.write(processedImage.getBytes());
            }
        }
    }
}