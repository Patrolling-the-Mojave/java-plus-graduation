package ru.yandex.practicum.graduation.core.event.dto.response.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private Float lat;
    private Float lon;
}
