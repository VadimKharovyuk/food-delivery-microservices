package com.example.deliveryproductservice.config;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        // Проверяем что все переменные заданы
        if (cloudName.isEmpty() || apiKey.isEmpty() || apiSecret.isEmpty()) {
            log.error("❌ Cloudinary configuration missing!");
            log.error("cloudName: '{}', apiKey: '{}', apiSecret: '{}'",
                    cloudName, apiKey.isEmpty() ? "EMPTY" : "SET", apiSecret.isEmpty() ? "EMPTY" : "SET");
            throw new RuntimeException("Cloudinary configuration is incomplete. Check your environment variables.");
        }

        log.info("🔧 Initializing Cloudinary with cloud name: {}", cloudName);

        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));

        log.info("✅ Cloudinary successfully initialized");
        return cloudinary;
    }
}
