package com.example.deliveryproductservice.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import jakarta.servlet.MultipartConfigElement;

/**
 * ✅ ИСПРАВЛЕННАЯ конфигурация multipart для Product Service
 */
@Configuration
public class MultipartConfig {

    /**
     * ✅ Конфигурация multipart с правильными лимитами
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();

        // Устанавливаем лимиты
        factory.setMaxFileSize(DataSize.ofMegabytes(10));      // 10MB на файл
        factory.setMaxRequestSize(DataSize.ofMegabytes(15));   // 15MB на запрос
        factory.setFileSizeThreshold(DataSize.ofKilobytes(1)); // 1KB порог

        return factory.createMultipartConfig();
    }

    /**
     * ✅ MultipartResolver для обработки multipart запросов
     */
    @Bean
    public MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        resolver.setResolveLazily(false); // Немедленная обработка
        return resolver;
    }

    /**
     * ✅ ДОБАВЛЕНО: Настройка Tomcat для увеличения лимита частей multipart
     */
    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();

        factory.addConnectorCustomizers(connector -> {
            // Увеличиваем лимиты Tomcat
            connector.setMaxPostSize(15 * 1024 * 1024); // 15MB
            connector.setMaxSavePostSize(15 * 1024 * 1024); // 15MB
        });

        // Настройка для обработки multipart с большим количеством частей
        factory.addContextCustomizers(context -> {
            // Устанавливаем системные свойства для Commons FileUpload
            System.setProperty("org.apache.tomcat.util.http.fileupload.impl.FileCountMax", "100");
            System.setProperty("org.apache.tomcat.util.http.fileupload.impl.FileSizeMax", "10485760"); // 10MB
            System.setProperty("org.apache.tomcat.util.http.fileupload.impl.SizeMax", "15728640"); // 15MB
        });

        return factory;
    }
}