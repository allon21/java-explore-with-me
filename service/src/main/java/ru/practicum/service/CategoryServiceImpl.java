package ru.practicum.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mappers.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ConflictException("Категория с именем " + newCategoryDto.getName() + " уже существует");
        }
        Category category = categoryRepository.save(CategoryMapper.INSTANCE.getCategory(newCategoryDto));
        return CategoryMapper.INSTANCE.getCategoryDto(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        final Category category = getCategoryIfExists(categoryId);
        if (eventRepository.existsByCategoryId(categoryId)) {
            throw new ConflictException("Невозможно удалить категорию, так как она связана с событиями.");
        }
        categoryRepository.deleteById(categoryId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long categoryId, NewCategoryDto newCategoryDto) {
        final Category category = getCategoryIfExists(categoryId);
        if (!category.getName().equals(newCategoryDto.getName())) {
            if (categoryRepository.existsByName(newCategoryDto.getName())) {
                throw new ConflictException("Категория с именем " + newCategoryDto.getName() + " уже существует");
            }
            category.setName(newCategoryDto.getName());
        }
        categoryRepository.save(category);
        return CategoryMapper.INSTANCE.getCategoryDto(category);
    }

    @Override
    public List<CategoryDto> getAllCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).stream()
                .map(CategoryMapper.INSTANCE::getCategoryDto)
                .toList();
    }

    @Override
    public CategoryDto getCategoryById(Long categoryId) {
        return CategoryMapper.INSTANCE.getCategoryDto(categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена")));
    }

    private Category getCategoryIfExists(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Не найдена категория с ID %d".formatted(id));
        }
        return categoryRepository.findById(id).orElseThrow();
    }
}
