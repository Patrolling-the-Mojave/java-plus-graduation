package ru.yandex.practicum.graduation.core.event.dto.request.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserIdAndEventIdDto {
    private Long userId;
    private Long eventId;

}
