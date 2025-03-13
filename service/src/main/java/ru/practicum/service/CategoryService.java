package ru.practicum.service;

import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategory(NewCategoryDto newCategoryDto);

    void deleteCategory(Long categoryId);

    CategoryDto updateCategory(Long categoryId, NewCategoryDto newCategoryDto);

    List<CategoryDto> getAllCategories(int from, int size);

    CategoryDto getCategoryById(Long categoryId);
}
