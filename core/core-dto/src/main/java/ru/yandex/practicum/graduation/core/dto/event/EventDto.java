package ru.yandex.practicum.graduation.core.dto.event;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.graduation.core.dto.EventStateDto;

@Data
@Builder
public class EventDto {
    private Long id;
    private Long initiatorId;
    private Integer participantLimit;
    private Boolean requestModeration;
    private EventStateDto state;
}
