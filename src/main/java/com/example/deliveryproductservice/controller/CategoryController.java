package com.example.deliveryproductservice.controller;

import com.example.deliveryproductservice.annotation.CurrentUser;
import com.example.deliveryproductservice.dto.category.CategoryResponseDto;
import com.example.deliveryproductservice.dto.category.CreateCategoryDto;
import com.example.deliveryproductservice.service.CategoryService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @PostConstruct
    public void init() {
        log.info("🚀 CategoryController initialized successfully");
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        log.info("🧪 CategoryController test endpoint called");
        return ResponseEntity.ok("CategoryController is working!");
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(
            @Valid @RequestBody CreateCategoryDto createCategoryDto,
            @CurrentUser Long userId,
            HttpServletRequest request) {

        log.info("🎯 CategoryController.createCategory called");

        // Дебаг всех заголовков
        String authHeader = request.getHeader("Authorization");
        log.info("🔍 Authorization header: {}", authHeader != null ? "Bearer ***" : "null");

        // Логируем информацию о пользователе из JWT
        String userEmail = request.getHeader("X-User-Email");
        String userRole = request.getHeader("X-User-Role");

        log.info("🔍 All custom headers - X-User-Id: {}, X-User-Email: {}, X-User-Role: {}",
                request.getHeader("X-User-Id"), userEmail, userRole);

        log.info("🔐 Creating category request from User: ID={}, Email={}, Role={}",
                userId, userEmail, userRole);

        // Проверяем права доступа (только ADMIN может создавать категории)
        if (!"ROLE_ADMIN".equals(userRole)) {
            log.warn("❌ Access denied for user {} with role {}", userId, userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            CategoryResponseDto createdCategory = categoryService.createCategory(createCategoryDto, userId);

            log.info("✅ Category created successfully: {} by user {}",
                    createdCategory.getName(), userId);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
        } catch (Exception e) {
            log.error("❌ Error creating category: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        log.info("📋 Fetching all active categories");
        try {
            List<CategoryResponseDto> categories = categoryService.getAllActiveCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("❌ Error fetching categories: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}