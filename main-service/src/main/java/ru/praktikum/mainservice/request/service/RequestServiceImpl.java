package ru.praktikum.mainservice.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.praktikum.mainservice.event.enums.StateEnum;
import ru.praktikum.mainservice.event.model.Event;
import ru.praktikum.mainservice.event.service.EventService;
import ru.praktikum.mainservice.exception.BadRequestException;
import ru.praktikum.mainservice.exception.NotFoundException;
import ru.praktikum.mainservice.request.mapper.RequestMapper;
import ru.praktikum.mainservice.request.model.Request;
import ru.praktikum.mainservice.request.model.dto.ParticipationRequestDto;
import ru.praktikum.mainservice.request.repository.RequestStorage;
import ru.praktikum.mainservice.user.model.User;
import ru.praktikum.mainservice.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestStorage requestStorage;
    private final EventService eventService;
    private final UserService userService;

    /**
     * POST REQUEST - Добавление запроса от текущего пользователя на участие в событии.
     * <p>
     * Обратите внимание:
     * <p>
     * - нельзя добавить повторный запрос;
     * <p>
     * - инициатор события не может добавить запрос на участие в своём событии;
     * <p>
     * - нельзя участвовать в неопубликованном событии;
     * <p>
     * - если у события достигнут лимит запросов на участие - необходимо вернуть ошибку;
     * <p>
     * - если для события отключена пре-модерация запросов на участие, то запрос должен автоматически перейти в состояние подтвержденного.
     *
     * @param userId  идентификатор пользователя.
     * @param eventId идентификатор события.
     * @return ParticipationRequestDto #{@link ParticipationRequestDto}
     */
    @Override
    public ParticipationRequestDto createRequest(long userId, long eventId) {

        Event event = eventService.checkStatusPublished(eventId);

        eventService.checkRequestLimitAndModeration(event);

        User currentUser = userService.checkUserAvailableInDb(userId);

        checkRequesterNotInitiator(event, currentUser);

        checkRepeatRequest(eventId, userId);

        // Создаем новый запрос и сетим данные;
        Request request = new Request();
        request.setRequester(currentUser);
        request.setEvent(event);
        request.setCreated(LocalDateTime.now());

        // Проверяем есть ли у события пре-модерация;
        if (event.getRequestModeration()) {
            request.setStatus("PENDING");
        } else {
            request.setStatus("CONFIRMED");
        }

        // Сохраняем запрос в БД и обновляем, чтобы записать id;
        request = requestStorage.save(request);

        log.info("Пользователь userId={} создает новый запрос а событие eventId={}", userId, eventId);
        return RequestMapper.fromRequestToParticipationRequestDto(request);
    }

    /**
     * PATCH REQUEST - Отмена своего запроса на участие в событии.
     *
     * @param userId    идентификатор пользователя;
     * @param requestId идентификатор запроса;
     * @return ParticipationRequestDto #{@link ParticipationRequestDto}
     */
    @Override
    public ParticipationRequestDto cancelOwnRequest(long userId, long requestId) {

        Request request = checkRequestAvailableInDb(requestId);
        request.setStatus(StateEnum.CANCELED.toString());
        requestStorage.save(request);

        log.info("Пользователь userId={} отменил запрос requestId={} на событие.", userId, requestId);
        return RequestMapper.fromRequestToParticipationRequestDto(request);
    }

    /**
     * GET REQUEST - Получение информации о заявках текущего пользователя на участие в чужих событиях.
     *
     * @param userId идентификатор пользователя;
     * @return возвращаем коллекцию ParticipationRequestDto #{@link ParticipationRequestDto}
     */
    @Override
    public List<ParticipationRequestDto> getRequests(long userId) {

        User user = userService.checkUserAvailableInDb(userId);

        List<Request> requests = requestStorage.findAllByRequester_Id(user.getId());

        log.info("Получаем информацию о всех заявках requests={} на событие пользователя userId={}.", requests.size(), userId);
        return requests.stream().map(RequestMapper::fromRequestToParticipationRequestDto).collect(Collectors.toList());
    }

    /**
     * Метод для проверки существования запроса в БД;
     *
     * @param requestId идентификатор запроса;
     * @return Request #{@link Request}
     */
    @Override
    public Request checkRequestAvailableInDb(long requestId) {

        return requestStorage.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String
                        .format("Запрос с таким requestId=%s не найден", requestId)));
    }

    /**
     * Метод проверяет что запрос создается впервые;
     *
     * @param eventId     идентификатор события;
     * @param requesterId идентификатор пользователя;
     */
    private void checkRepeatRequest(long eventId, long requesterId) {

        if (requestStorage.findRequestByEvent_IdAndRequester_Id(eventId, requesterId).isPresent()) {
            throw new BadRequestException(String.format("Повторный запрос от пользователя " +
                    "requesterId=%s на событие eventId=%s", requesterId, eventId));
        }
    }

    /**
     * Метод проверяет что пользователь не является инициатором события.
     *
     * @param event     событие;
     * @param requester пользователь;
     */
    private void checkRequesterNotInitiator(Event event, User requester) {

        if (event.getInitiator().equals(requester)) {
            throw new BadRequestException(String.format("Запрос не может быть создан инициатором " +
                    "события requesterId=%s", requester.getId()));
        }
    }

    /**
     * Метод проверяет что пользователь имеет подтвержденный запрос на участие в событии;
     *
     * @param eventId     идентификатор события;
     * @param requesterId идентификатор пользователя;
     */
    @Override
    public void checkRequesterHasConfirmedRequest(long eventId, long requesterId) {

        requestStorage.findRequestByEvent_IdAndRequester_IdAndStatus(eventId, requesterId, "CONFIRMED")
                .orElseThrow(() -> new BadRequestException(String.format("Пользователь requesterId=%s не имеет " +
                        "подтвержденного запроса в данном событии eventId=%s", requesterId, eventId)));
    }

}
