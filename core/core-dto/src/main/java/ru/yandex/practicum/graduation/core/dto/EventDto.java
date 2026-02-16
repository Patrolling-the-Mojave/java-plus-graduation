package ru.yandex.practicum.graduation.core.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventDto {
    private Long id;
    private Long initiatorId;
    private Integer participantLimit;
    private Boolean requestModeration;
    private EventStateDto state;
}
