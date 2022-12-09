package ru.praktikum.mainservice.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.praktikum.mainservice.event.model.dto.AdminUpdateEventRequest;
import ru.praktikum.mainservice.event.model.dto.EventFullDto;
import ru.praktikum.mainservice.event.service.EventService;
import ru.praktikum.mainservice.event.utils.EventFilterValidDates;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class EventAdminController {

    private final EventService eventService;
    private final EventFilterValidDates eventFilterValidDates;

    /**
     * GET EVENT ADMIN - Поиск событий.
     * <p>
     * Эндпоинт возвращает полную информацию обо всех событиях подходящих под переданные условия.
     *
     * @param users      коллекция идентификаторов пользователей;
     * @param states     коллекция статусов событий;
     * @param categories коллекция категорий;
     * @param rangeStart начальная дата поиска событий;
     * @param rangeEnd   окончательная дата поиска событий;
     * @param from       с какой страницы показываем результаты поиска;
     * @param size       какое количество результатов на страницу показываем;
     * @return возвращаем коллекцию из EventFullDto #{@link EventFullDto}
     */
    @GetMapping
    public List<EventFullDto> searchEvents(@RequestParam @Nullable List<Long> users,
                                           @RequestParam @Nullable List<String> states,
                                           @RequestParam @Nullable List<Long> categories,
                                           @RequestParam @Nullable String rangeStart,
                                           @RequestParam @Nullable String rangeEnd,
                                           @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                           @Positive @RequestParam(defaultValue = "10") Integer size) {

        Map<String, LocalDateTime> dates = eventFilterValidDates.checkAndFormat(rangeStart, rangeEnd);

        log.info("Получаем все события с учетом параметров: users={}, states={}, categories={}, " +
                        "start={}, end={}, from={}, size={}",
                users,
                states,
                categories,
                dates.get("start"),
                dates.get("end"),
                from,
                size);

        List<EventFullDto> result = eventService.searchEvents(
                users,
                states,
                categories,
                dates.get("start"),
                dates.get("end"),
                from,
                size);

        log.info("Найденные события: result={}", result);
        return result;
    }

    /**
     * PUT EVENT ADMIN - Редактирование события.
     * <p>
     * Редактирование данных любого события администратором. Валидация данных не требуется.
     *
     * @param eventId                 идентификатор события;
     * @param adminUpdateEventRequest DTO для редактирования события администратором;
     * @return EventFullDto #{@link EventFullDto}
     */
    @PutMapping("/{eventId}")
    public EventFullDto updateEventByAdmin(@PathVariable long eventId,
                                           @RequestBody AdminUpdateEventRequest adminUpdateEventRequest) {

        log.info("Админ редактирует событие: eventId={}", eventId);
        return eventService.updateEventByAdmin(eventId, adminUpdateEventRequest);
    }

    /**
     * PUT EVENT ADMIN - Публикация события.
     * <p>
     * Обратите внимание:
     * <p>
     * - дата начала события должна быть не ранее чем за час от даты публикации;
     * <p>
     * - событие должно быть в состоянии ожидания публикации;
     *
     * @param eventId идентификатор события;
     * @return EventFullDto #{@link EventFullDto}
     */
    @PatchMapping("/{eventId}/publish")
    public EventFullDto eventPublishByAdmin(@PathVariable long eventId) {

        log.info("Админ подтверждает событие и публикует его: eventId={}", eventId);
        return eventService.eventPublishByAdmin(eventId);
    }

    /**
     * PUT EVENT ADMIN - Отклонение события.
     * <p>
     * Обратите внимание:
     * <p>
     * - событие не должно быть опубликовано;
     *
     * @param eventId идентификатор события;
     * @return EventFullDto #{@link EventFullDto}
     */
    @PatchMapping("/{eventId}/reject")
    public EventFullDto eventRejectByAdmin(@PathVariable long eventId) {

        log.info("Админ отклоняет событие и публикует его: eventId={}", eventId);
        return eventService.eventRejectByAdmin(eventId);
    }
}
