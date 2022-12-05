package ru.praktikum.mainservice.category.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.praktikum.mainservice.category.model.Category;
import ru.praktikum.mainservice.category.model.dto.CategoryDto;
import ru.praktikum.mainservice.category.model.dto.NewCategoryDto;

@Slf4j
@Service
public class CategoryMapper {

    public static Category newCategoryDtoToCategory(NewCategoryDto newCategoryDto) {

        Category category = new Category();
        category.setName(newCategoryDto.getName());

        log.info("Маппим NewCategoryDto в Category");
        return category;
    }

    public static CategoryDto categoryToCategoryDto(Category category) {

        log.info("Маппим Category в CategoryDto");
        return new CategoryDto(category.getId(), category.getName());
    }

}
