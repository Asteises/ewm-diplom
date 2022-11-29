package ru.praktikum.mainservice.compilations.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.praktikum.mainservice.compilations.mapper.CompilationMapper;
import ru.praktikum.mainservice.compilations.model.Compilation;
import ru.praktikum.mainservice.compilations.model.CompilationEvent;
import ru.praktikum.mainservice.compilations.model.dto.CompilationDto;
import ru.praktikum.mainservice.compilations.model.dto.NewCompilationDto;
import ru.praktikum.mainservice.compilations.repository.CompilationEventStorage;
import ru.praktikum.mainservice.compilations.repository.CompilationStorage;
import ru.praktikum.mainservice.event.mapper.EventMapper;
import ru.praktikum.mainservice.event.model.Event;
import ru.praktikum.mainservice.event.model.dto.EventShortDto;
import ru.praktikum.mainservice.event.service.EventService;
import ru.praktikum.mainservice.exception.BadRequestException;
import ru.praktikum.mainservice.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationStorage compilationStorage;

    private final CompilationEventStorage compilationEventStorage;

    private final EventService eventService;

    /*
    GET COMPILATION - Получение подборок событий
    */
    @Override
    public List<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size) {

        List<Compilation> compilations;

        // Проверяем все ли параметры пришли;
        if (pinned != null) {
            // Если да, то собираем лист всех подборок по заданным параметрам;
            compilations = compilationStorage
                    .findAllByPinned(pinned, PageRequest.of(from / size, size))
                    .stream()
                    .collect(Collectors.toList());
        } else {
            // Если нет, то собираем все что есть в БД;
            compilations = compilationStorage.findAll(PageRequest.of(from / size, size))
                    .stream()
                    .collect(Collectors.toList());
        }

        // Создаем результирующий объект;
        List<CompilationDto> result = new ArrayList<>();

        // Для каждой подборки создаем CompilationDto;
        for (Compilation compilation : compilations) {

            // Используем метод getCompilationById;
            CompilationDto compilationDto = getCompilationById(compilation.getId());

            // Записываем в результирующий список;
            result.add(compilationDto);
        }

        log.info("Получаем все подборки pinned={}, from={}, size={}", pinned, from, size);
        return result;
    }

    /*
    GET COMPILATION - Получение подборки по id
    */
    @Override
    public CompilationDto getCompilationById(long compId) {

        // Проверяем существует подборка или нет;
        Compilation compilation = checkCompilationAvailableInBd(compId);

        // Находим все CompilationEvent связанные с этой подборкой;
        List<CompilationEvent> compilationEvents = compilationEventStorage.findAllByComp(compilation);

        // Находим все события, связанные с этой подборкой;
        List<Event> events = new ArrayList<>();
        for (CompilationEvent compilationEvent : compilationEvents) {
            events.add(compilationEvent.getEvent());
        }

        // Мапим CompilationDto;
        CompilationDto result = CompilationMapper.fromCompToCompDto(compilation);

        // Сетим подборки;
        result.setEvents(events.stream()
                .map(EventMapper::fromEventToEventShortDto)
                .collect(Collectors.toList()));

        log.info("Получаем подборку compId={}", compId);
        return result;
    }

    /*
    POST COMPILATION - Добавление новой подборки
    */
    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {

        // Создаем новую категорию и сохраняем в БД;
        Compilation compilation = CompilationMapper.fromNewCompToCom(newCompilationDto);
        compilationStorage.save(compilation);

        // Мапим результирующий объект;
        CompilationDto result = CompilationMapper.fromCompToCompDto(compilation);

        // Находим все события по пришедшим id;
        List<Event> events = eventService.getEventsByIds(newCompilationDto.getEvents());

        // Сохраняем подборки вместе с событиями в CompilationEvent;
        for (Event event : events) {

            // Проверяем каждое событие на существование;
            event = eventService.checkEventAvailableInDb(event.getId());

            // Создаем CompilationEvent, мапим и сохраняем в БД;
            CompilationEvent compilationEvent = CompilationMapper.toCompilationEvent(compilation, event);
            compilationEventStorage.save(compilationEvent);
        }

        // Мапим события в лист EventShortDto;
        List<EventShortDto> eventShortDtos = events.stream()
                .map(EventMapper::fromEventToEventShortDto).collect(Collectors.toList());

        // Сетим события в результат;
        result.setEvents(eventShortDtos);

        log.info("Подборка успешно создана result={}", result);
        return result;
    }

    /*
    DELETE COMPILATION - Удаление подборки
     */
    @Override
    public void deleteCompilation(long compId) {

        // Проверяем существование подборки;
        Compilation compilation = checkCompilationAvailableInBd(compId);

        // Собираем все события участвующие в подборке;
        List<CompilationEvent> compilationsEvents = compilationEventStorage.findAllByComp(compilation);

        // Удаляем все события из подборки;
        compilationEventStorage.deleteAll(compilationsEvents);

        // Удаляем подборку;
        log.info("Удаляем подборку compId={}", compId);
        compilationStorage.delete(compilation);
    }

    /*
    DELETE COMPILATION - Удаление событие из подборки
    */
    @Override
    public void deleteEventFromCompilation(long compId, long eventId) {

        // Проверяем существование подборки;
        checkCompilationAvailableInBd(compId);

        // Проверяем существование события;
        eventService.checkEventAvailableInDb(eventId);

        // Проверяем есть ли в подборке это событие;
        CompilationEvent compilationEvent = compilationEventStorage
                .findByEvent_IdAndAndComp_Id(eventId, compId)
                .orElseThrow(() -> new BadRequestException("CompilationEvent не найден"));

        // И удаляем его из БД;
        log.info("Удаляем событие eventId={} из подборки compId={}", eventId, compId);
        compilationEventStorage.delete(compilationEvent);
    }

    /*
    PATCH COMPILATION - Добавить событие в подборку
    */
    @Override
    public void addEventInCompilation(long compId, long eventId) {

        // Проверяем существование подборки;
        Compilation compilation = checkCompilationAvailableInBd(compId);

        // Проверяем существование события;
        Event event = eventService.checkEventAvailableInDb(eventId);

        // Создаем новый CompilationEvent, чтобы сохранить связь события и подборки;
        CompilationEvent compilationEvent = new CompilationEvent();

        // Сетим данные;
        compilationEvent.setComp(compilation);
        compilationEvent.setEvent(event);

        // Сохраняем в БД;
        log.info("Добавляем событие eventId={} в подборку compId={}: compilationEvent={}", eventId, compId, compilationEvent);
        compilationEventStorage.save(compilationEvent);
    }

    /*
    DELETE COMPILATION - Открепить подборку на главной странице
    */
    @Override
    public void unpinCompilationAtHomePage(long compId) {

        // Проверяем существование подборки;
        Compilation compilation = checkCompilationAvailableInBd(compId);

        // Открепляем;
        compilation.setPinned(false);

        log.info("Открепили подборку compId={} : {}", compId, compilation.getPinned());
        compilationStorage.save(compilation);
    }

    /*
    PATCH COMPILATION - Закрепить подборку на главной странице
    */
    @Override
    public void pinCompilationAtHomePage(long compId) {

        // Проверяем существование подборки;
        Compilation compilation = checkCompilationAvailableInBd(compId);

        // Прикрепляем;
        compilation.setPinned(true);

        log.info("Закрепили подборку compId={} : {}", compId, compilation.getPinned());
        compilationStorage.save(compilation);
    }


    /*
    Метод для проверки наличия подборки в БД
    */
    private Compilation checkCompilationAvailableInBd(long compId) {

        log.info("Проверяем существование подборки compId={}", compId);
        return compilationStorage.findById(compId).orElseThrow(() -> new NotFoundException(
                String.format("Подборка не найдена: compId=%s", compId)));
    }

}
