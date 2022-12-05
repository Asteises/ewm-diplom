package ru.praktikum.mainservice.compilations.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.praktikum.mainservice.compilations.model.Compilation;
import ru.praktikum.mainservice.compilations.model.dto.CompilationDto;
import ru.praktikum.mainservice.compilations.model.dto.NewCompilationDto;
import ru.praktikum.mainservice.event.mapper.EventMapper;
import ru.praktikum.mainservice.event.model.Event;
import ru.praktikum.mainservice.event.model.dto.EventShortDto;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CompilationMapper {

    public static CompilationDto fromCompToCompDto(Compilation compilation) {

        CompilationDto compDto = new CompilationDto();
        List<EventShortDto> events = new ArrayList<>();

        compDto.setId(compilation.getId());
        compDto.setPinned(compilation.getPinned());
        compDto.setTitle(compilation.getTitle());
        for (Event event : compilation.getEvents()) {
            events.add(EventMapper.fromEventToEventShortDto(event));
        }
        compDto.setEvents(events);

        return compDto;
    }

    public static Compilation fromNewCompToCom(NewCompilationDto newCompilationDto) {

        Compilation compilation = new Compilation();

        compilation.setPinned(newCompilationDto.getPinned());
        compilation.setTitle(newCompilationDto.getTitle());

        return compilation;
    }
}
