package ru.praktikum.mainservice.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.praktikum.mainservice.client.StatClient;
import ru.praktikum.mainservice.event.model.dto.EventFullDto;
import ru.praktikum.mainservice.event.model.dto.EventShortDto;
import ru.praktikum.mainservice.event.service.EventService;
import ru.praktikum.mainservice.event.utils.EventFilterValidDates;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventPublicController {

    private final EventService eventService;
    private final StatClient statClient;
    private final EventFilterValidDates eventFilterValidDates;

    // TODO
    /*
    GET EVENTS - Получение событий с возможностью фильтрации
        Обратите внимание:
            + это публичный эндпоинт, соответственно в выдаче должны быть только опубликованные события;
            + текстовый поиск (по аннотации и подробному описанию) должен быть без учета регистра букв;
            + если в запросе не указан диапазон дат [rangeStart-rangeEnd], то нужно выгружать события, которые произойдут позже текущей даты и времени;
            + информация о каждом событии должна включать в себя количество просмотров и количество уже одобренных заявок на участие;
            + информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики;
     */
    @GetMapping()
    public List<EventShortDto> getAllPublicEvents(@RequestParam @Nullable String text,
                                                  @RequestParam @Nullable List<Long> categories,
                                                  @RequestParam(defaultValue = "false") @Nullable Boolean paid,
                                                  @RequestParam @Nullable String rangeStart,
                                                  @RequestParam @Nullable String rangeEnd,
                                                  @RequestParam(defaultValue = "false") @Nullable Boolean onlyAvailable,
                                                  @RequestParam(defaultValue = "EVENT_DATE") @Nullable String sort, // Вариант сортировки: по дате события или по количеству просмотров Available values : EVENT_DATE, VIEWS
                                                  @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                  @Positive @RequestParam(defaultValue = "10") Integer size,
                                                  HttpServletRequest request) {

        // Валидируем время;
        Map<String, LocalDateTime> dates = eventFilterValidDates.checkAndFormat(rangeStart, rangeEnd);

        log.info("Получаем все события с учетом фильтрации: text={}, categories={}, paid={}, start={}, " +
                        "end={}, onlyAvailable={}, sort={}, from={}, size={}",
                text, categories, paid, dates.get("start"), dates.get("end"), onlyAvailable, sort, from, size);

        // Информация для сервиса статистики;
        log.info("client ip: {}", request.getRemoteAddr());
        log.info("endpoint path: {}", request.getRequestURI());
        statClient.saveRequestInfo(request);

        List<EventShortDto> result = eventService.getAllPublicEvents(
                text,
                categories,
                paid,
                dates.get("start"),
                dates.get("end"),
                sort,
                from,
                size);

        log.info("Получаем результат: result={}", result);
        return result;
    }

    /*
    Получение подробной информации об опубликованном событии по его идентификатору
        Обратите внимание:
            событие должно быть опубликовано;
            информация о событии должна включать в себя количество просмотров и количество подтвержденных запросов;
            информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики;
     */
    @GetMapping("/{id}")
    public EventFullDto getPublicEventById(@PathVariable long id,
                                           HttpServletRequest request) {

        // Информация для сервиса статистики;
        log.info("client ip: {}", request.getRemoteAddr());
        log.info("endpoint path: {}", request.getRequestURI());
        statClient.saveRequestInfo(request);

        EventFullDto eventFullDto = eventService.getPublicEventById(id);

        log.info("Получаем событие: eventId={}", id);
        return eventFullDto;
    }
}
