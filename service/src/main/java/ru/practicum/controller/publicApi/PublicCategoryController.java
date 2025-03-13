package ru.practicum.controller.publicApi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class PublicCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories(@RequestParam(defaultValue = "0") int from,
                                                           @RequestParam(defaultValue = "10") int size) {
        List<CategoryDto> categories = categoryService.getAllCategories(from, size);
        return ResponseEntity.status(HttpStatus.OK).body(categories);
    }

    @GetMapping("/{catId}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long catId) {
        CategoryDto categoryDto = categoryService.getCategoryById(catId);
        return ResponseEntity.status(HttpStatus.OK).body(categoryDto);
    }
}
