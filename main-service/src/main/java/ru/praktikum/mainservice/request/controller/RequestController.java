package ru.praktikum.mainservice.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.praktikum.mainservice.request.model.dto.ParticipationRequestDto;
import ru.praktikum.mainservice.request.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class RequestController {

    private final RequestService requestService;

    /*
    POST REQUEST - Добавление запроса от текущего пользователя на участие в событии
        Обратите внимание:
            + нельзя добавить повторный запрос;
            + инициатор события не может добавить запрос на участие в своём событии;
            + нельзя участвовать в неопубликованном событии;
            + если у события достигнут лимит запросов на участие - необходимо вернуть ошибку;
            + если для события отключена пре-модерация запросов на участие, то запрос должен автоматически перейти в состояние подтвержденного;
     */
    @PostMapping("/{userId}/requests")
    public ParticipationRequestDto createRequest(@PathVariable Long userId,
                                                 @RequestParam long eventId) {

        log.info("Пользователь userId={} создает новый запрос а событие eventId={}", userId, eventId);
        return requestService.createRequest(userId, eventId);
    }

    /*
    PATCH REQUEST - Отмена своего запроса на участие в событии
     */
    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelOwnRequest(@PathVariable long userId,
                                                    @PathVariable long requestId) {

        log.info("Пользователь userId={} отменяет свой запрос requestId={} на событие", userId, requestId);
        return requestService.cancelOwnRequest(userId, requestId);
    }

    /*
    GET REQUEST - Получение информации о заявках текущего пользователя на участие в чужих событиях
    */
    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> getRequests(@PathVariable long userId) {

        log.info("Пользователь userId={} получает все свои запросы", userId);
        return requestService.getRequests(userId);
    }
}
