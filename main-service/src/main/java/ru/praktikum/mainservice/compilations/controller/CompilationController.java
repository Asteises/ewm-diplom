package ru.praktikum.mainservice.compilations.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.praktikum.mainservice.compilations.model.dto.CompilationDto;
import ru.praktikum.mainservice.compilations.service.CompilationService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/compilations")
public class CompilationController {

    private final CompilationService compilationService;

    /*
    GET COMPILATION - Получение подборок событий
    */
    @GetMapping()
    public List<CompilationDto> getAllCompilations(@RequestParam @Nullable Boolean pinned,
                                                   @PositiveOrZero @RequestParam(defaultValue = "0") @Nullable Integer from,
                                                   @Positive @RequestParam(defaultValue = "10") @Nullable Integer size) {

        log.info("Получаем все подборки с параметрами: pinned={}, from={}, size={}", pinned, from, size);
        return compilationService.getAllCompilations(pinned, from, size);
    }

    /*
    GET COMPILATION - Получение подборки по id
    */
    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable long compId) {

        log.info("Получаем подборку compId={}", compId);
        return compilationService.getCompilationById(compId);
    }
}
