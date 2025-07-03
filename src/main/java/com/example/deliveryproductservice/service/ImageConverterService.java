// ImageConverterService.java - ЗАВЕРШЕННАЯ ВЕРСИЯ
package com.example.deliveryproductservice.service;

import com.example.deliveryproductservice.Exception.ImageConversionException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

@Service
@Slf4j
public class ImageConverterService {

    private static final Set<String> CONVERTIBLE_TYPES = Set.of(
            "image/heif",
            "image/heic"
    );

    private static final String TARGET_FORMAT = "jpeg";
    private static final String TARGET_CONTENT_TYPE = "image/jpeg";
    private static final String TARGET_EXTENSION = ".jpg";

    // Настройки для продуктов
    private static final int PRODUCT_IMAGE_MAX_WIDTH = 1200;
    private static final int PRODUCT_IMAGE_MAX_HEIGHT = 1200;
    private static final float JPEG_QUALITY = 0.85f;

    public boolean needsConversion(String contentType) {
        return CONVERTIBLE_TYPES.contains(contentType.toLowerCase());
    }

    /**
     * Основной метод для обработки изображений продуктов
     */
    public ProcessedImage processProductImage(MultipartFile imageFile) throws IOException {
        try {
            log.info("Processing product image: {}, type: {}, size: {} bytes",
                    imageFile.getOriginalFilename(),
                    imageFile.getContentType(),
                    imageFile.getSize());

            // 1. Проверяем, нужна ли конвертация из HEIF/HEIC
            if (needsConversion(imageFile.getContentType())) {
                log.info("Converting HEIF/HEIC to JPEG for file: {}", imageFile.getOriginalFilename());
                ProcessedImage convertedImage = convertToStandardFormat(imageFile);

                // Дополнительно обрабатываем размер, если нужно
                return resizeIfNeeded(convertedImage);
            }

            // 2. Для обычных форматов - проверяем размер и оптимизируем
            return processStandardImage(imageFile);

        } catch (Exception e) {
            log.error("Failed to process product image {}: {}",
                    imageFile.getOriginalFilename(), e.getMessage(), e);
            throw new IOException("Failed to process product image: " + e.getMessage(), e);
        }
    }

    /**
     * Обрабатывает стандартные форматы изображений (JPG, PNG, GIF)
     */
    private ProcessedImage processStandardImage(MultipartFile imageFile) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageFile.getBytes()));

        if (originalImage == null) {
            throw new IOException("Could not read image file: " + imageFile.getOriginalFilename());
        }

        // Проверяем, нужно ли изменить размер
        boolean needsResize = originalImage.getWidth() > PRODUCT_IMAGE_MAX_WIDTH ||
                originalImage.getHeight() > PRODUCT_IMAGE_MAX_HEIGHT;

        if (!needsResize && !"image/png".equals(imageFile.getContentType())) {
            // Изображение уже подходящего размера и не PNG - возвращаем как есть
            log.info("Image {} doesn't need processing", imageFile.getOriginalFilename());
            return ProcessedImage.fromMultipartFile(imageFile);
        }

        // Обрабатываем изображение
        BufferedImage processedImage = needsResize ?
                resizeImageWithAspectRatio(originalImage, PRODUCT_IMAGE_MAX_WIDTH, PRODUCT_IMAGE_MAX_HEIGHT) :
                originalImage;

        // Конвертируем в JPEG для оптимизации
        byte[] optimizedBytes = convertToOptimizedJpeg(processedImage);
        String newFileName = generateJpegFileName(imageFile.getOriginalFilename());

        log.info("Processed standard image: {} -> {}, original size: {} bytes, new size: {} bytes",
                imageFile.getOriginalFilename(), newFileName, imageFile.getSize(), optimizedBytes.length);

        return ProcessedImage.builder()
                .bytes(optimizedBytes)
                .contentType(TARGET_CONTENT_TYPE)
                .fileName(newFileName)
                .extension(TARGET_EXTENSION)
                .originalFileName(imageFile.getOriginalFilename())
                .originalContentType(imageFile.getContentType())
                .build();
    }

    /**
     * Дополнительно обрабатывает уже сконвертированное изображение
     */
    private ProcessedImage resizeIfNeeded(ProcessedImage convertedImage) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(convertedImage.getBytes()));

        if (image.getWidth() <= PRODUCT_IMAGE_MAX_WIDTH &&
                image.getHeight() <= PRODUCT_IMAGE_MAX_HEIGHT) {
            return convertedImage; // Размер уже подходящий
        }

        BufferedImage resizedImage = resizeImageWithAspectRatio(
                image, PRODUCT_IMAGE_MAX_WIDTH, PRODUCT_IMAGE_MAX_HEIGHT);

        byte[] resizedBytes = convertToOptimizedJpeg(resizedImage);

        return ProcessedImage.builder()
                .bytes(resizedBytes)
                .contentType(convertedImage.getContentType())
                .fileName(convertedImage.getFileName())
                .extension(convertedImage.getExtension())
                .originalFileName(convertedImage.getOriginalFileName())
                .originalContentType(convertedImage.getOriginalContentType())
                .build();
    }

    /**
     * Изменяет размер изображения с сохранением пропорций
     */
    private BufferedImage resizeImageWithAspectRatio(BufferedImage originalImage, int maxWidth, int maxHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Вычисляем новые размеры с сохранением пропорций
        double aspectRatio = (double) originalWidth / originalHeight;
        int newWidth, newHeight;

        if (originalWidth > originalHeight) {
            newWidth = Math.min(maxWidth, originalWidth);
            newHeight = (int) (newWidth / aspectRatio);

            if (newHeight > maxHeight) {
                newHeight = maxHeight;
                newWidth = (int) (newHeight * aspectRatio);
            }
        } else {
            newHeight = Math.min(maxHeight, originalHeight);
            newWidth = (int) (newHeight * aspectRatio);

            if (newWidth > maxWidth) {
                newWidth = maxWidth;
                newHeight = (int) (newWidth / aspectRatio);
            }
        }

        // Создаем новое изображение
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();

        // Настройки для качественного масштабирования
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Заливаем белым фоном
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, newWidth, newHeight);

        // Масштабируем изображение
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        log.info("Resized image from {}x{} to {}x{}",
                originalWidth, originalHeight, newWidth, newHeight);

        return resizedImage;
    }

    /**
     * Конвертирует изображение в оптимизированный JPEG
     */
    private byte[] convertToOptimizedJpeg(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Создаем RGB изображение если нужно
            BufferedImage rgbImage = image;
            if (image.getType() != BufferedImage.TYPE_INT_RGB) {
                rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = rgbImage.createGraphics();
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
                g2d.drawImage(image, 0, 0, null);
                g2d.dispose();
            }

            // Настраиваем качество JPEG
            ImageWriter writer = ImageIO.getImageWritersByFormatName(TARGET_FORMAT).next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionQuality(JPEG_QUALITY);

            // Записываем в поток
            ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
            writer.setOutput(imageOutputStream);
            writer.write(null, new IIOImage(rgbImage, null, null), writeParam);

            writer.dispose();
            imageOutputStream.close();

            return outputStream.toByteArray();
        }
    }

    public ProcessedImage convertToStandardFormat(MultipartFile file) {
        try {
            log.info("Converting file {} from {} to JPEG",
                    file.getOriginalFilename(), file.getContentType());

            // Читаем исходное изображение
            BufferedImage sourceImage = readHeifImage(file);

            // Конвертируем в JPEG
            byte[] convertedBytes = convertToJpeg(sourceImage);

            // Генерируем новое имя файла
            String newFileName = generateNewFileName(file.getOriginalFilename());

            log.info("Successfully converted {} to JPEG, size: {} bytes",
                    file.getOriginalFilename(), convertedBytes.length);

            return ProcessedImage.builder()
                    .bytes(convertedBytes)
                    .contentType(TARGET_CONTENT_TYPE)
                    .fileName(newFileName)
                    .extension(TARGET_EXTENSION)
                    .originalFileName(file.getOriginalFilename())
                    .originalContentType(file.getContentType())
                    .build();

        } catch (Exception e) {
            log.error("Failed to convert image {}: {}",
                    file.getOriginalFilename(), e.getMessage(), e);
            throw new ImageConversionException(
                    "Failed to convert HEIF image to JPEG: " + e.getMessage(), e);
        }
    }

    private BufferedImage readHeifImage(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);

            if (image == null) {
                throw new ImageConversionException(
                        "Unable to read HEIF image. ImageIO returned null");
            }

            return image;
        }
    }

    private byte[] convertToJpeg(BufferedImage sourceImage) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Создаем новое изображение с RGB цветовым пространством
            BufferedImage rgbImage = new BufferedImage(
                    sourceImage.getWidth(),
                    sourceImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB
            );

            // Копируем изображение, убирая прозрачность если есть
            Graphics2D g2d = rgbImage.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
            g2d.drawImage(sourceImage, 0, 0, null);
            g2d.dispose();

            // Настраиваем качество JPEG
            ImageWriter writer = ImageIO.getImageWritersByFormatName(TARGET_FORMAT).next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionQuality(0.9f);

            // Записываем в поток
            ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
            writer.setOutput(imageOutputStream);
            writer.write(null, new IIOImage(rgbImage, null, null), writeParam);

            writer.dispose();
            imageOutputStream.close();

            return outputStream.toByteArray();
        }
    }

    private String generateNewFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isEmpty()) {
            return "converted_image" + TARGET_EXTENSION;
        }

        String nameWithoutExtension = originalFileName.replaceAll("\\.[^.]*$", "");
        return nameWithoutExtension + TARGET_EXTENSION;
    }

    private String generateJpegFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isEmpty()) {
            return "product_image" + TARGET_EXTENSION;
        }

        String nameWithoutExtension = originalFileName.replaceAll("\\.[^.]*$", "");
        return nameWithoutExtension + TARGET_EXTENSION;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProcessedImage {
        private byte[] bytes;
        private String contentType;
        private String fileName;
        private String extension;
        private String originalFileName;
        private String originalContentType;

        public static ProcessedImage fromMultipartFile(MultipartFile file) {
            try {
                return ProcessedImage.builder()
                        .bytes(file.getBytes())
                        .contentType(file.getContentType())
                        .fileName(file.getOriginalFilename())
                        .extension(getExtensionFromFileName(file.getOriginalFilename()))
                        .originalFileName(file.getOriginalFilename())
                        .originalContentType(file.getContentType())
                        .build();
            } catch (IOException e) {
                throw new RuntimeException("Failed to read file bytes", e);
            }
        }

        private static String getExtensionFromFileName(String fileName) {
            if (fileName == null || !fileName.contains(".")) {
                return "";
            }
            return fileName.substring(fileName.lastIndexOf("."));
        }

        public long getSize() {
            return bytes != null ? bytes.length : 0;
        }
    }
}