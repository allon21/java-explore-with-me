package ru.practicum.controller.adminApi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.service.CompilationService;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
public class AdminCompilationController {

    private final CompilationService compilationService;

    @PostMapping
    public ResponseEntity<CompilationDto> addCompilation(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        CompilationDto compilationDto = compilationService.addCompilation(newCompilationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(compilationDto);
    }

    @DeleteMapping("/{compId}")
    public ResponseEntity<Void> deleteCompilation(@PathVariable Long compId) {
        compilationService.deleteCompilation(compId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{compId}")
    public ResponseEntity<CompilationDto> updateCompilation(@PathVariable Long compId,
                                                            @Valid @RequestBody UpdateCompilationRequest updateCompilationRequest) {
        CompilationDto compilationDto = compilationService.updateCompilation(compId, updateCompilationRequest);
        return ResponseEntity.status(HttpStatus.OK).body(compilationDto);
    }
}

