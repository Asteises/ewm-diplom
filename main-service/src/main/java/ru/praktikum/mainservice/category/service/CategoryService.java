package ru.praktikum.mainservice.category.service;

import org.springframework.stereotype.Service;
import ru.praktikum.mainservice.category.model.Category;
import ru.praktikum.mainservice.category.model.dto.CategoryDto;
import ru.praktikum.mainservice.category.model.dto.NewCategoryDto;

import java.util.List;

@Service
public interface CategoryService {

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    CategoryDto updateCategory(CategoryDto categoryDto);

    void deleteCategory(long catId);

    List<CategoryDto> getAllCategories(Integer from, Integer size);

    CategoryDto getCategoryById(long catId);

    Category checkCategory(long catId);
}
