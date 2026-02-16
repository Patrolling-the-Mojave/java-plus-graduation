package ru.yandex.practicum.graduation.core.event.service;

import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.graduation.core.event.dto.response.compilation.CompilationDto;

import java.util.List;

public interface CompilationPublicService {
    CompilationDto findById(Long compilationId);

    List<CompilationDto> findAllByFilters(Boolean pinned, Pageable pageable);


}
