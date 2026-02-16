package ru.yandex.practicum.graduation.core.event.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.graduation.core.event.dto.request.category.NewCategoryDto;
import ru.yandex.practicum.graduation.core.event.dto.response.category.CategoryDto;
import ru.yandex.practicum.graduation.core.event.model.Category;


@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "id", ignore = true)
    Category toEntity(NewCategoryDto newCategoryDto);

    CategoryDto toDto(Category category);
}
