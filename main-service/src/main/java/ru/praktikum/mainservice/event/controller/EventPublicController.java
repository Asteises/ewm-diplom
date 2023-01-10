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
import ru.praktikum.mainservice.comment.model.dto.CommentDto;
import ru.praktikum.mainservice.comment.service.CommentService;
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
    private final CommentService commentService;
    private final StatClient statClient;

    private final EventFilterValidDates eventFilterValidDates;

    /**
     * GET EVENTS - Получение событий с возможностью фильтрации.
     * <p>
     * Обратите внимание:
     * <p>
     * - это публичный эндпоинт, соответственно в выдаче должны быть только опубликованные события;
     * <p>
     * - текстовый поиск (по аннотации и подробному описанию) должен быть без учета регистра букв;
     * <p>
     * - если в запросе не указан диапазон дат [rangeStart-rangeEnd], то нужно выгружать события, которые произойдут позже текущей даты и времени;
     * <p>
     * - информация о каждом событии должна включать в себя количество просмотров и количество уже одобренных заявок на участие;
     * <p>
     * - информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики;
     *
     * @param text          пользовательский текст для поиска событий;
     * @param categories    коллекция из идентификаторов категорий;
     * @param paid          платное или бесплатное событие;
     * @param rangeStart    с какой даты ищем начало событий;
     * @param rangeEnd      по какую дату ищем начало событий;
     * @param onlyAvailable только доступные события;
     * @param sort          варианты сортировки: по дате события (по умолчанию) или по количеству просмотров события;
     * @param from          с какой страницы будем показывать результаты;
     * @param size          количество результатов на странице;
     * @param request       #{@link HttpServletRequest}
     * @return возвращаем коллекцию из #{@link EventFullDto}
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

    /**
     * Получение подробной информации об опубликованном событии по его идентификатору.
     * <p>
     * Обратите внимание:
     * <p>
     * - событие должно быть опубликовано;
     * <p>
     * - информация о событии должна включать в себя количество просмотров и количество подтвержденных запросов;
     * <p>
     * - информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики;
     *
     * @param id      идентификатор события;
     * @param request #{@link HttpServletRequest}
     * @return EventFullDto #{@link EventFullDto}
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

    /**
     * GET COMMENT - Получить комментарий по id.
     * <p>
     * Обратите внимание:
     * <p>
     * - комментарии доступны для всех статусов событий;
     *
     * @param id        идентификатор события;
     * @param commentId идентификатор комментария;
     * @return CommentDto #{@link CommentDto}
     */
    @GetMapping("/{id}/comments/{commentId}")
    public CommentDto getPublicCommentById(@PathVariable long id,
                                           @PathVariable long commentId) {

        log.info("Получаем комментарий commentId={} на событие eventId={}", commentId, id);
        return commentService.getCommentById(id, commentId);
    }

    /**
     * GET COMMENT - Получить все комментарии по eventId.
     * <p>
     * Обратите внимание:
     * <p>
     * - комментарии доступны для всех статусов событий;
     *
     * @param id идентификатор события;
     * @return возвращаем коллекцию из #{@link CommentDto}
     */
    @GetMapping("/{id}/comments")
    public List<CommentDto> getAllPublicCommentsByEventId(@PathVariable long id) {

        log.info("Получаем все комментарии на событие eventId={}", id);
        return commentService.getAllCommentsByEventId(id);
    }
}
