package ru.yandex.practicum.graduation.core.event.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.graduation.core.dto.exception.NotFoundException;
import ru.yandex.practicum.graduation.core.event.dto.request.category.NewCategoryDto;
import ru.yandex.practicum.graduation.core.event.dto.response.category.CategoryDto;
import ru.yandex.practicum.graduation.core.event.mapper.CategoryMapper;
import ru.yandex.practicum.graduation.core.event.model.Category;
import ru.yandex.practicum.graduation.core.event.repository.CategoryRepository;
import ru.yandex.practicum.graduation.core.event.service.CategoryService;


import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImp implements CategoryService {
    private final CategoryRepository repository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryDto> getCategories(Pageable pageable) {
        List<Category> categories = repository.findAll(pageable).stream().toList();
        return categories.stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryDto getCategory(Long catId) {
        return categoryMapper.toDto(repository.findById(catId).orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found")));
    }

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        log.info("Добавление новой категории " + newCategoryDto);
        Category category = categoryMapper.toEntity(newCategoryDto);
        Category savedCategory = repository.save(category);
        log.info("Категория добавлена: {}", savedCategory);
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        log.info("Попытка удаления категории по ID: {}", catId);
        if (!repository.existsById(catId)) {
            throw new NotFoundException("Category with id=" + catId + " was not found");
        }
        repository.deleteById(catId);
        log.info("Категория с ID: {} удалена", catId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(NewCategoryDto newCategoryDto, Long catId) {
        log.info("Попытка обновления категории с ID " + catId);
        Category existingCategory = repository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        existingCategory.setName(newCategoryDto.getName());
        Category updatedCategory = repository.save(existingCategory);
        log.info("Категория обновлена: {}", updatedCategory);
        return categoryMapper.toDto(updatedCategory);
    }
}
