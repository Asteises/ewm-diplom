package ru.praktikum.mainservice.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.praktikum.mainservice.category.model.dto.CategoryDto;
import ru.praktikum.mainservice.category.service.CategoryService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryPublicController {

    private final CategoryService categoryService;

    /*
    GET CATEGORIES - Получение всех категорий;
     */
    @GetMapping
    public List<CategoryDto> getAllCategories(@PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                              @Positive @RequestParam(defaultValue = "10") Integer size) {

        log.info("Получаем все категории с параметрами: from={}, size={}", from, size);
        return categoryService.getAllCategories(from, size);
    }

    /*
    GET CATEGORIES - Получение категории по id;
     */
    @GetMapping("/{catId}")
    public CategoryDto getCategoryById(@PathVariable @PositiveOrZero long catId) {

        log.info("Получаем категорию: catId={}", catId);
        return categoryService.getCategoryById(catId);
    }
}
