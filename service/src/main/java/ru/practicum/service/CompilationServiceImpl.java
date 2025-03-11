package ru.practicum.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mappers.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.repository.CompilationRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = CompilationMapper.INSTANCE.getCompilation(newCompilationDto);
        compilation = compilationRepository.save(compilation);
        return CompilationMapper.INSTANCE.getCompilationDto(compilation);
    }

    @Override
    public void deleteCompilation(Long compilationId) {
        if (!compilationRepository.existsById(compilationId)) {
            throw new NotFoundException("Compilation не найдено");
        } else {
            compilationRepository.deleteById(compilationId);
        }
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compilationId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Compilation не найдено"));
        CompilationMapper.INSTANCE.update(compilation, updateCompilationRequest);
        return CompilationMapper.INSTANCE.getCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    public List<CompilationDto> getByFilterPublic(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);

        return compilationRepository.findAllByFilterPublic(pinned, pageable).stream()
                .map(CompilationMapper.INSTANCE::getCompilationDto)
                .toList();
    }

    @Override
    public CompilationDto getCompilationById(Long compilationId) {
        return CompilationMapper.INSTANCE.getCompilationDto(compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Compilation не найдено")));
    }
}
