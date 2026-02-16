package ru.yandex.practicum.graduation.core.event.service;


import ru.yandex.practicum.graduation.core.event.dto.request.compilation.NewCompilationDto;
import ru.yandex.practicum.graduation.core.event.dto.request.compilation.UpdateCompilationRequest;
import ru.yandex.practicum.graduation.core.event.dto.response.compilation.CompilationDto;

public interface CompilationAdminService {

    CompilationDto add(NewCompilationDto newCompilation);

    void deleteById(Long compilationId);

    CompilationDto update(Long compilationId, UpdateCompilationRequest updatedCompilation);
}
