package ru.praktikum.mainservice.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.praktikum.mainservice.category.model.dto.CategoryDto;
import ru.praktikum.mainservice.category.model.dto.NewCategoryDto;
import ru.praktikum.mainservice.category.service.CategoryService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class CategoryAdminController {

    private final CategoryService categoryService;

    /*
    POST CATEGORIES - Добавление новой категорий
        Обратите внимание:
            + имя категории должно быть уникальным;
    */
    @PostMapping
    public CategoryDto createCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {

        log.info("Создаем новую категорию {}", newCategoryDto.toString());
        return categoryService.createCategory(newCategoryDto);
    }

    /*
    PATCH CATEGORIES - Добавление новой категорий
        Обратите внимание:
            + имя категории должно быть уникальным;
    */
    @PatchMapping
    public CategoryDto updateCategory(@Valid @RequestBody CategoryDto categoryDto) {

        log.info("Меняем категорию: categoryId={}, name={}", categoryDto.getId(), categoryDto.getName());
        return categoryService.updateCategory(categoryDto);
    }

    /*
    DELETE CATEGORIES - Добавление новой категорий
        Обратите внимание:
            + с категорией не должно быть связано ни одного события;
    */
    @DeleteMapping("/{catId}")
    public void deleteUser(@PathVariable long catId) {

        log.info("Удаляем категорию catId={}", catId);
        categoryService.deleteCategory(catId);
    }
}
