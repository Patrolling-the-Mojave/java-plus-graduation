package ru.yandex.practicum.graduation.core.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.graduation.core.event.dto.request.compilation.NewCompilationDto;
import ru.yandex.practicum.graduation.core.event.dto.request.compilation.UpdateCompilationRequest;
import ru.yandex.practicum.graduation.core.event.dto.response.compilation.CompilationDto;
import ru.yandex.practicum.graduation.core.event.service.CompilationAdminService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/compilations")
public class CompilationAdminController {
    private final CompilationAdminService compilationAdminService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilation(@Validated @RequestBody NewCompilationDto newCompilation) {
        return compilationAdminService.add(newCompilation);
    }

    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(@PathVariable Long compId,
                                            @Validated @RequestBody UpdateCompilationRequest updatedCompilation) {
        return compilationAdminService.update(compId, updatedCompilation);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        compilationAdminService.deleteById(compId);
    }
}
