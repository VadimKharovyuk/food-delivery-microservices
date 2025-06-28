// 1. TestCategoryController.java - для тестирования
package com.example.deliveryproductservice.controller;

import com.example.deliveryproductservice.dto.category.CategoryResponseDto;
import com.example.deliveryproductservice.dto.category.CreateCategoryDto;
import com.example.deliveryproductservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/test/categories")
@RequiredArgsConstructor
@Slf4j
public class TestCategoryController {

    private final CategoryService categoryService;

    /**
     * 📋 Главная страница со списком категорий и формой создания
     */
    @GetMapping
    public String categoryTestPage(Model model) {
        try {
            List<CategoryResponseDto> categories = categoryService.getAllActiveCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("newCategory", new CreateCategoryDto());

            log.info("📋 Loaded {} categories for test page", categories.size());
        } catch (Exception e) {
            log.error("❌ Error loading categories: {}", e.getMessage(), e);
            model.addAttribute("error", "Ошибка загрузки категорий: " + e.getMessage());
        }

        return "test-categories";
    }

    /**
     * ➕ Создание новой категории
     */
    @PostMapping("/create")
    public String createCategory(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam(value = "sortOrder", defaultValue = "0") Integer sortOrder,
            @RequestParam(value = "isActive", defaultValue = "true") Boolean isActive,
            RedirectAttributes redirectAttributes) {

        try {
            CreateCategoryDto dto = new CreateCategoryDto();
            dto.setName(name);
            dto.setDescription(description);
            dto.setImageFile(imageFile.isEmpty() ? null : imageFile);
            dto.setSortOrder(sortOrder);
            dto.setIsActive(isActive);

            // Для тестирования используем фиксированный userId = 1 (ADMIN)
            CategoryResponseDto created = categoryService.createCategory(dto, 1L);

            log.info("✅ Category created via test form: {}", created.getName());
            redirectAttributes.addFlashAttribute("success",
                    "Категория '" + created.getName() + "' успешно создана!");

        } catch (Exception e) {
            log.error("❌ Error creating category: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка создания категории: " + e.getMessage());
        }

        return "redirect:/test/categories";
    }

    /**
     * 🗑️ Удаление категории
     */
    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id, 1L); // Фиксированный userId для теста

            log.info("🗑️ Category {} deleted via test form", id);
            redirectAttributes.addFlashAttribute("success", "Категория удалена!");

        } catch (Exception e) {
            log.error("❌ Error deleting category {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка удаления категории: " + e.getMessage());
        }

        return "redirect:/test/categories";
    }

    /**
     * 📊 API endpoint для получения категорий в JSON (для AJAX)
     */
    @GetMapping("/api")
    @ResponseBody
    public List<CategoryResponseDto> getCategoriesApi() {
        return categoryService.getAllActiveCategories();
    }
}