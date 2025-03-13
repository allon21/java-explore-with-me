package ru.practicum.service;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto addCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long compilationId);

    CompilationDto updateCompilation(Long compilationId, UpdateCompilationRequest updateCompilationRequest);

    List<CompilationDto> getByFilterPublic(Boolean pinned, int from, int size);

    CompilationDto getCompilationById(Long compilationId);
}
