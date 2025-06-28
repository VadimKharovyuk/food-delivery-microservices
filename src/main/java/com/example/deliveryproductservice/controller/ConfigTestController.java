package com.example.deliveryproductservice.controller;


import com.example.deliveryproductservice.config.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class ConfigTestController {

    @Value("${cloudinary.cloud-name:NOT_SET}")
    private String cloudName;

    @Value("${cloudinary.api-key:NOT_SET}")
    private String apiKey;

    @Value("${cloudinary.api-secret:NOT_SET}")
    private String apiSecret;

    @Value("${mapbox.access.token:NOT_SET}")
    private String mapboxToken;

    private final JwtUtil jwtUtil;

    @GetMapping("/cloudinary-config")
    public Map<String, Object> getCloudinaryConfig() {
        return Map.of(
                "cloudName", cloudName,
                "apiKeySet", !apiKey.equals("NOT_SET") && !apiKey.isEmpty(),
                "apiSecretSet", !apiSecret.equals("NOT_SET") && !apiSecret.isEmpty(),
                "mapboxTokenSet", !mapboxToken.equals("NOT_SET") && !mapboxToken.isEmpty(),
                "status", "OK"
        );
    }

    @GetMapping("/jwt-info")
    public Map<String, Object> getJwtInfo(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        return Map.of(
                "userIdFromJWT", userId != null ? userId : "NOT_SET",
                "emailFromJWT", email != null ? email : "NOT_SET",
                "roleFromJWT", role != null ? role : "NOT_SET",
                "jwtFilterWorking", userId != null
        );
    }

    @GetMapping("/debug-token")
    public Map<String, String> debugToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            jwtUtil.debugToken(token);
            return Map.of("message", "Token details logged to console");
        }
        return Map.of("error", "No Authorization header found");
    }
}