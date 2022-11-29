package ru.praktikum.mainservice.compilations.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.praktikum.mainservice.compilations.model.dto.CompilationDto;
import ru.praktikum.mainservice.compilations.model.dto.NewCompilationDto;
import ru.praktikum.mainservice.compilations.service.CompilationService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/compilations")
public class CompilationAdminController {

    private final CompilationService compilationService;

    /*
    POST COMPILATION - Добавление новой подборки
     */
    @PostMapping
    public CompilationDto createCompilation(@Valid @RequestBody NewCompilationDto newCompilationDto) {

        log.info("Создаем новую подборку: newCompilationDto={}", newCompilationDto.toString());
        return compilationService.createCompilation(newCompilationDto);
    }

    /*
    DELETE COMPILATION - Удаление подборки
     */
    @DeleteMapping("/{compId}")
    public void deleteCompilation(@PathVariable long compId) {

        log.info("Удаляем подборку: compId={}", compId);
        compilationService.deleteCompilation(compId);
    }

    /*
    DELETE COMPILATION - Удаление события из подборки
     */
    @DeleteMapping("/{compId}/events/{eventId}")
    public void deleteEventFromCompilation(@PathVariable long compId,
                                           @PathVariable long eventId) {

        log.info("Удаляем событие eventId={} из подборки: compId={}", eventId, compId);
        compilationService.deleteEventFromCompilation(compId, eventId);
    }

    /*
    PATCH COMPILATION - Добавить событие в подборку
    */
    @PatchMapping("/{compId}/events/{eventId}")
    public void addEventInCompilation(@PathVariable long compId,
                                      @PathVariable long eventId) {

        log.info("Добавляем событие eventId={} в подборку: compId={}", eventId, compId);
        compilationService.addEventInCompilation(compId, eventId);
    }

    /*
    DELETE COMPILATION - Открепить подборку на главной странице
    */
    @DeleteMapping("/{compId}/pin")
    public void unpinCompilationAtHomePage(@PathVariable long compId) {

        log.info("Открепляем подборку на главной странице: compId={}", compId);
        compilationService.unpinCompilationAtHomePage(compId);
    }

    /*
    PATCH COMPILATION - Закрепить подборку на главной странице
    */
    @PatchMapping("/{compId}/pin")
    public void pinCompilationAtHomePage(@PathVariable long compId) {

        log.info("Закрепляем подборку на главной странице: compId={}", compId);
        compilationService.pinCompilationAtHomePage(compId);
    }
}
