package ru.praktikum.mainservice.compilations.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.praktikum.mainservice.compilations.model.Compilation;
import ru.praktikum.mainservice.compilations.model.CompilationEvent;
import ru.praktikum.mainservice.compilations.model.dto.CompilationDto;
import ru.praktikum.mainservice.compilations.model.dto.NewCompilationDto;
import ru.praktikum.mainservice.event.model.Event;

import java.util.ArrayList;

@Slf4j
@Service
public class CompilationMapper {

    public static CompilationDto fromCompToCompDto(Compilation compilation) {

        CompilationDto compDto = new CompilationDto();

        compDto.setEvents(new ArrayList<>());
        compDto.setId(compilation.getId());
        compDto.setPinned(compilation.getPinned());
        compDto.setTitle(compilation.getTitle());

        return compDto;
    }

    public static Compilation fromNewCompToCom(NewCompilationDto newCompilationDto) {

        Compilation compilation = new Compilation();

        compilation.setPinned(newCompilationDto.getPinned());
        compilation.setTitle(newCompilationDto.getTitle());

        return compilation;
    }

    public static CompilationEvent toCompilationEvent(Compilation compilation, Event event) {

        CompilationEvent compilationEvent = new CompilationEvent();

        compilationEvent.setComp(compilation);
        compilationEvent.setEvent(event);

        return compilationEvent;
    }
}
