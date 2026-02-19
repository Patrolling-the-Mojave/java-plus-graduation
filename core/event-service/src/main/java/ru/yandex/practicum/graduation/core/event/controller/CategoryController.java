package ru.yandex.practicum.graduation.core.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.graduation.core.event.dto.request.category.NewCategoryDto;
import ru.yandex.practicum.graduation.core.event.dto.response.category.CategoryDto;
import ru.yandex.practicum.graduation.core.event.service.CategoryService;


import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class CategoryController {
    private final CategoryService service;

    @GetMapping("/categories")
    public List<CategoryDto> getCategories(@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                           @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(from, size);
        return service.getCategories(pageable);
    }

    @GetMapping("/categories/{catId}")
    public CategoryDto getCategory(@PathVariable @Positive Long catId) {
        return service.getCategory(catId);
    }

    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        return service.addCategory(newCategoryDto);
    }

    @DeleteMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable @Positive Long catId) {
        service.deleteCategory(catId);
    }

    @PatchMapping("/admin/categories/{catId}")
    public CategoryDto updateCategory(@Valid @RequestBody NewCategoryDto newCategoryDto,
                                      @PathVariable @Positive Long catId) {
        return service.updateCategory(newCategoryDto, catId);
    }
}
