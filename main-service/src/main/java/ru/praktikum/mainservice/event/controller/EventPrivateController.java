package ru.praktikum.mainservice.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.praktikum.mainservice.event.model.dto.EventFullDto;
import ru.praktikum.mainservice.event.model.dto.NewEventDto;
import ru.praktikum.mainservice.event.service.EventService;
import ru.praktikum.mainservice.request.model.dto.ParticipationRequestDto;
import ru.praktikum.mainservice.request.model.dto.UpdateEventRequest;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class EventPrivateController {

    private final EventService eventService;

    /*
    POST EVENT - Добавление нового события:
        Обратите внимание:
            + дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента;
    */
    @PostMapping("/{userId}/events")
    public EventFullDto createEvent(@PathVariable long userId,
                                    @Valid @RequestBody NewEventDto newEventDto) {

        log.info("Пользователь userId={} создает новое событие {}", userId, newEventDto.toString());
        return eventService.createEvent(userId, newEventDto);
    }

    /*
    PATCH EVENT - Изменение события добавленного текущим пользователем:
        Обратите внимание:
            + изменить можно только отмененные события или события в состоянии ожидания модерации;
            + если редактируется отменённое событие, то оно автоматически переходит в состояние ожидания модерации;
            + дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента;
    */
    @PatchMapping("/{userId}/events")
    public EventFullDto updateEventByCurrentUser(@PathVariable long userId,
                                                 @Valid @RequestBody UpdateEventRequest updateEventRequest) {

        log.info("Пользователь userId={} обновляет событие {}", userId, updateEventRequest.toString());
        return eventService.updateEventByCurrentUser(userId, updateEventRequest);
    }

    /*
    GET EVENTS - Получение событий добавленных текущим пользователем:
    */
    @GetMapping("/{userId}/events")
    public List<EventFullDto> getAllEventsByCurrentUser(@PathVariable long userId,
                                                        @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                        @Positive @RequestParam(defaultValue = "10") Integer size) {

        log.info("Пользователь userId={} получает все свои созданные события", userId);
        return eventService.getAllEventsByCurrentUser(userId, from, size);
    }

    /*
    GET EVENT - Получение полной информации о событии добавленном текущим пользователем:
    */
    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto getEventByIdByCurrentUser(@PathVariable long userId,
                                                  @PathVariable long eventId) {

        log.info("Пользователь userId={} получает свое событие eventId={}", userId, eventId);
        return eventService.getEventByIdByCurrentUser(userId, eventId);
    }

    /*
    PATCH EVENT - Отмена события добавленного текущим пользователем:
        Обратите внимание:
            + Отменить можно только событие в состоянии ожидания модерации;
     */
    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto cancelEventByCurrentUser(@PathVariable long userId,
                                                 @PathVariable long eventId) {

        log.info("Пользователь userId={} отменяет свое событие eventId={}", userId, eventId);
        return eventService.cancelEventByCurrentUser(userId, eventId);
    }

    /*
    GET EVENT - Получение информации о запросах на участие в событии текущего пользователя:
    */
    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsByEventByCurrentUser(@PathVariable long userId,
                                                                         @PathVariable long eventId) {

        log.info("Пользователь userId={} получает все запросы на свое событие eventId={}", userId, eventId);
        return eventService.getRequestsByEventByCurrentUser(userId, eventId);
    }

    /*
    PATCH EVENT - Подтверждение чужой заявки на участие в событии текущего пользователя:
        Обратите внимание:
            + если для события лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется;
            + нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие;
            + если при подтверждении данной заявки, лимит заявок для события исчерпан, то все неподтверждённые заявки необходимо отклонить;
     */
    @PatchMapping("/{userId}/events/{eventId}/requests/{reqId}/confirm")
    public ParticipationRequestDto acceptRequestOnEventByCurrentUser(@PathVariable long userId,
                                                                     @PathVariable long eventId,
                                                                     @PathVariable long reqId) {

        log.info("Пользователь userId={} одобряет чужой запрос reqId={} на участие свое событие eventId={}",
                userId, reqId, eventId);
        return eventService.acceptRequestOnEventByCurrentUser(userId, eventId, reqId);
    }

    /*
    PATCH EVENT - Отклонение чужой заявки на участие в событии текущего пользователя:
     */
    @PatchMapping("/{userId}/events/{eventId}/requests/{reqId}/reject")
    public ParticipationRequestDto cancelRequestOnEventByCurrentUser(@PathVariable long userId,
                                                                     @PathVariable long eventId,
                                                                     @PathVariable long reqId) {

        log.info("Пользователь userId={} одобряет чужой запрос reqId={} на участие свое событие eventId={}",
                userId, reqId, eventId);
        return eventService.cancelRequestOnEventByCurrentUser(userId, eventId, reqId);
    }
}
