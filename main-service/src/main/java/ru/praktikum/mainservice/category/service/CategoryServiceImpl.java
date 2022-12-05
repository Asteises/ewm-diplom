package ru.praktikum.mainservice.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.praktikum.mainservice.category.mapper.CategoryMapper;
import ru.praktikum.mainservice.category.model.Category;
import ru.praktikum.mainservice.category.model.dto.CategoryDto;
import ru.praktikum.mainservice.category.model.dto.NewCategoryDto;
import ru.praktikum.mainservice.category.repository.CategoryStorage;
import ru.praktikum.mainservice.event.repository.EventStorage;
import ru.praktikum.mainservice.exception.BadRequestException;
import ru.praktikum.mainservice.exception.ConflictException;
import ru.praktikum.mainservice.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryStorage categoryStorage;
    private final EventStorage eventStorage;

    /*
    POST CATEGORIES - Добавление новой категорий
        Обратите внимание:
            + имя категории должно быть уникальным;
    */
    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {

        // Проверяем уникальность названия категории;
        checkNameCategory(newCategoryDto.getName());

        // Мапим;
        Category category = CategoryMapper.newCategoryDtoToCategory(newCategoryDto);

        // Сохраняем и перезаписываем, чтобы записался id;
        category = categoryStorage.save(category);

        log.info("Создана новая категория: catId={}, catName={}", category.getId(), category.getName());
        return CategoryMapper.categoryToCategoryDto(category);
    }

    /*
    PATCH CATEGORIES - Изменение категории
        Обратите внимание:
            + имя категории должно быть уникальным;
    */
    @Override
    public CategoryDto updateCategory(CategoryDto categoryDto) {

        // Проверяем наличие категории;
        Category category = checkCategory(categoryDto.getId());

        if (category.getName().equals(categoryDto.getName())) {
            throw new BadRequestException("Оказалось, что менять то и нечего");
        }

        // Проверяем уникальность имени категории;
        checkNameCategory(categoryDto.getName());

        // Сетим данные и обновляем БД;
        category.setName(categoryDto.getName());
        categoryStorage.save(category);

        log.info("Внесли изменения в категорию catId={}, изменили имя на catName={}", categoryDto.getId(), categoryDto.getName());
        return CategoryMapper.categoryToCategoryDto(category);
    }

    /*
    DELETE CATEGORIES - Добавление новой категорий
        Обратите внимание:
            + с категорией не должно быть связано ни одного события;
    */
    @Override
    public void deleteCategory(long catId) {

        // Проверяем наличие категории;
        Category category = checkCategory(catId);

        // Проверяем есть ли связанные с категорией события;
        if (eventStorage.findEventByCategory_Id(catId).isPresent()) {
            throw new BadRequestException("Категорию нельзя удалить, так как есть связанные с ней события");
        }

        log.info("Категория удалена: category={}", category.toString());
        categoryStorage.deleteById(catId);
    }

    /*
    GET CATEGORIES - Получение всех категорий;
    */
    @Override
    public List<CategoryDto> getAllCategories(Integer from, Integer size) {

        // Записываем найденные в БД категории;
        List<Category> categories = categoryStorage.findAll(PageRequest.of(from / size, size)).toList();

        log.info("Получаем все категории с параметрами: from={}, size={}", from, size);
        return categories.stream().map(CategoryMapper::categoryToCategoryDto).collect(Collectors.toList());
    }

    /*
    GET CATEGORIES - Получение категории по id;
     */
    @Override
    public CategoryDto getCategoryById(long catId) {

        // Проверяем наличие категории;
        Category category = checkCategory(catId);

        log.info("Получаем категорию: category={}", category.toString());
        return CategoryMapper.categoryToCategoryDto(category);
    }

    @Override
    public Category checkCategory(long catId) {

        log.info("Проверяем существование категории catId={}", catId);
        return categoryStorage.findById(catId)
                .orElseThrow(() -> new NotFoundException(String.format("Категория не найдена: categoryId=%s", catId)));
    }

    /*
    Метод проверяет уникальность имени категории;
     */
    private void checkNameCategory(String catName) {

        log.info("Проверяем уникальность имени категории catName={}", catName);

        if (categoryStorage.findCategoryByName(catName).isPresent()) {
            log.info("Категория с таким именем уже существует!");
            throw new ConflictException(String.format("Категория с таким именем catName=%s уже существует", catName));
        }
    }
}
