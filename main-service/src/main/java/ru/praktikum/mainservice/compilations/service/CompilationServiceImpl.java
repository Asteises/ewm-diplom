package ru.praktikum.mainservice.compilations.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.praktikum.mainservice.compilations.mapper.CompilationMapper;
import ru.praktikum.mainservice.compilations.model.Compilation;
import ru.praktikum.mainservice.compilations.model.dto.CompilationDto;
import ru.praktikum.mainservice.compilations.model.dto.NewCompilationDto;
import ru.praktikum.mainservice.compilations.repository.CompilationStorage;
import ru.praktikum.mainservice.event.model.Event;
import ru.praktikum.mainservice.event.service.EventService;
import ru.praktikum.mainservice.exception.BadRequestException;
import ru.praktikum.mainservice.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationStorage compilationStorage;

    private final EventService eventService;

    /*
    GET COMPILATION - Получение подборок событий
    */
    @Override
    public List<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size) {

        List<CompilationDto> compilations;

        // Проверяем все ли параметры пришли;
        if (pinned != null) {
            // Если да, то собираем лист всех подборок по заданным параметрам;
            compilations = compilationStorage
                    .findAllByPinned(pinned, PageRequest.of(from / size, size))
                    .stream()
                    .map(CompilationMapper::fromCompToCompDto)
                    .collect(Collectors.toList());
        } else {
            // Если нет, то собираем все что есть в БД;
            compilations = compilationStorage.findAll(PageRequest.of(from / size, size))
                    .stream()
                    .map(CompilationMapper::fromCompToCompDto)
                    .collect(Collectors.toList());
        }

        log.info("Получаем все подборки compilations={}", compilations);
        return compilations;
    }

    /*
    GET COMPILATION - Получение подборки по id
    */
    @Override
    public CompilationDto getCompilationById(long compId) {

        // Проверяем существует подборка или нет;
        Compilation compilation = checkCompilationAvailableInBd(compId);

        // Мапим CompilationDto;
        CompilationDto result = CompilationMapper.fromCompToCompDto(compilation);

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

        // Собираем все подборки по id которые пришли в newCompilationDto;
        List<Event> events = eventService.getEventsByIds(newCompilationDto.getEvents());

        // Сетим подборки в Compilation и сохраняем в БД;
        compilation.setEvents(events);
        compilationStorage.save(compilation);

        // Мапим результирующий объект;
        CompilationDto result = CompilationMapper.fromCompToCompDto(compilation);

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
        Compilation compilation = checkCompilationAvailableInBd(compId);

        // Проверяем существование события;
        Event event = eventService.checkEventAvailableInDb(eventId);

        // Проверяем есть ли в подборке это событие;
        if (!compilation.getEvents().contains(event)) {
            throw new BadRequestException(String.format("Данного события eventId=%s нет в подборке compId=%s", eventId, compId));
        }

        // Удаляем событие из подборки и сохраняем обновленные данные в БД;
        compilation.getEvents().remove(event);
        compilationStorage.save(compilation);

        log.info("Удаляем событие eventId={} из подборки compId={}", eventId, compId);
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

        // Добавляем событие в подборку;
        compilation.getEvents().add(event);

        // Сохраняем в БД;
        log.info("Добавляем событие eventId={} в подборку compId={}", eventId, compId);
        compilationStorage.save(compilation);
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
