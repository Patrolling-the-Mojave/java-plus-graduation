package ru.yandex.practicum.graduation.core.event.dto.request.event;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.graduation.core.event.dto.response.request.RequestStatusDto;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {

    @NotEmpty(message = "Список идентификаторов запросов не может быть пустым")
    private Set<Long> requestIds;

    @NotNull(message = "Статус не может быть null")
    private RequestStatusDto status;
}
