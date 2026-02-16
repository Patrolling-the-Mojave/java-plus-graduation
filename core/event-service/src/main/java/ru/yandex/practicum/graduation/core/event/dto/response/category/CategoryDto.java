package ru.yandex.practicum.graduation.core.event.dto.response.category;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CategoryDto {
    private Long id;
    private String name;
}
