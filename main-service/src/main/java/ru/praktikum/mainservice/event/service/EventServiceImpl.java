package ru.praktikum.mainservice.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.praktikum.mainservice.category.model.Category;
import ru.praktikum.mainservice.category.service.CategoryService;
import ru.praktikum.mainservice.client.StatClient;
import ru.praktikum.mainservice.client.dto.ViewStatsDto;
import ru.praktikum.mainservice.event.enums.StateEnum;
import ru.praktikum.mainservice.event.mapper.EventMapper;
import ru.praktikum.mainservice.event.model.Event;
import ru.praktikum.mainservice.event.model.dto.AdminUpdateEventRequest;
import ru.praktikum.mainservice.event.model.dto.EventFullDto;
import ru.praktikum.mainservice.event.model.dto.EventShortDto;
import ru.praktikum.mainservice.event.model.dto.NewEventDto;
import ru.praktikum.mainservice.event.repository.EventStorage;
import ru.praktikum.mainservice.exception.BadRequestException;
import ru.praktikum.mainservice.exception.NotFoundException;
import ru.praktikum.mainservice.location.Location;
import ru.praktikum.mainservice.location.LocationService;
import ru.praktikum.mainservice.request.mapper.RequestMapper;
import ru.praktikum.mainservice.request.model.Request;
import ru.praktikum.mainservice.request.model.dto.ParticipationRequestDto;
import ru.praktikum.mainservice.request.model.dto.UpdateEventRequest;
import ru.praktikum.mainservice.request.repository.RequestStorage;
import ru.praktikum.mainservice.user.model.User;
import ru.praktikum.mainservice.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventStorage eventStorage;
    private final UserService userService;
    private final CategoryService categoryService;
    private final LocationService locationService;
    private final RequestStorage requestStorage;
    private final StatClient statClient;

    /**
     * POST EVENT - Добавление нового события:
     * <p>
     * Обратите внимание:
     * <p>
     * - дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента;
     *
     * @param userId      идентификатор пользователя;
     * @param newEventDto #{@link NewEventDto}
     * @return EventFullDto #{@link EventFullDto}
     */
    @Override
    public EventFullDto createEvent(long userId, NewEventDto newEventDto) {

        // Создаем переменную времени события;
        LocalDateTime eventDate = LocalDateTime.parse(newEventDto.getEventDate(), (EventMapper.FORMATTER_EVENT_DATE));

        // Валидируем время события;
        checkEventCreateDate(eventDate);

        // Создаем новую локацию и сохраняем ее в БД, чтобы получить id;
        Location newLocation = newEventDto.getLocation();
        newLocation = locationService.createLocation(newLocation);

        /*
        Из контроллера приходит только id пользователя,
        а положить в Event нужно всего пользователя, дополнительно проверяем наличие пользователя в БД;
        */
        User initiator = userService.checkUserAvailableInDb(userId);

        /*
        В NewEventDto приходит только id категории,
        а в Event нужно будет положить всю категорию, дополнительно проверяем наличие категории в БД;
        */
        Category category = categoryService.checkCategory(newEventDto.getCategory());

        // Мапим событие и сетим пользователя, категорию, локацию и статус;
        Event event = EventMapper.toEvent(newEventDto);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(newLocation);
        event.setState(StateEnum.PENDING.toString());

        // Обновляем Event, так как после сохранения в БД у него появился id;
        event = eventStorage.save(event);

        log.info("Создано новое событие: {}", event);
        return EventMapper.fromEventToEventFullDto(event);
    }

    /**
     * PATCH EVENT - Изменение события добавленного текущим пользователем.
     * <p>
     * Обратите внимание:
     * <p>
     * - изменить можно только отмененные события или события в состоянии ожидания модерации;
     * <p>
     * - если редактируется отменённое событие, то оно автоматически переходит в состояние ожидания модерации;
     * <p>
     * - дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента;
     *
     * @param userId идентификатор пользователя;
     * @param updateEventRequest DTO для редактирования события;
     * @return EventFullDto #{@link EventFullDto}
     */
    @Override
    public EventFullDto updateEventByCurrentUser(long userId, UpdateEventRequest updateEventRequest) {

        // Создаем переменную времени события;
        LocalDateTime eventDate = LocalDateTime.parse(updateEventRequest.getEventDate(), (EventMapper.FORMATTER_EVENT_DATE));

        // Валидируем время события;
        checkEventCreateDate(eventDate);

        User currentUser = userService.checkUserAvailableInDb(userId);

        Event currentEvent = checkEventAvailableInDb(updateEventRequest.getEventId());

        checkOwnEvent(currentEvent, currentUser);

        // Проверяем чтобы событие не было опубликовано;
        if (currentEvent.getState().equals(StateEnum.PUBLISHED.toString())) {
            throw new BadRequestException(String.format("Событие eventId=%s нельзя изменить, так как оно опубликовано", currentEvent.getId()));
        }

        // Если State был CANCELED, то меняем на PENDING, сохраняем изменения для eventState;
        if (currentEvent.getState().equals(StateEnum.CANCELED.toString())) {
            currentEvent.setState(StateEnum.PENDING.toString());
        }

        // Сначала Мапим ответ, все что пришло в updateEventRequest;
        EventMapper.fromUpdateEventRequestToEvent(currentEvent, updateEventRequest);

        // Добавляем юзера;
        currentEvent.setInitiator(currentUser);

        // Так как новые данные нужно будет сохранить в БД, то нужна вся категория, а не просто catId;
        if (updateEventRequest.getCategory() != null) {
            // Проверяем категорию;
            Category category = categoryService.checkCategory(updateEventRequest.getCategory());
            currentEvent.setCategory(category);
        }

        // Обновляем данные в БД;
        eventStorage.save(currentEvent);

        // Мапим результирующий объект;
        EventFullDto result = EventMapper.fromEventToEventFullDto(currentEvent);

        log.info("Событие изменено: {}", result);
        return result;
    }

    /**
     * GET EVENTS - Получение событий добавленных текущим пользователем.
     *
     * @param userId идентификатор пользователя;
     * @param from   какой страницы начнем просматривать события;
     * @param size   какое количество событий будем выводить на странице;
     * @return возвращаем коллекцию из EventFullDto #{@link EventFullDto}
     */
    @Override
    public List<EventFullDto> getAllEventsByCurrentUser(long userId, Integer from, Integer size) {

        User user = userService.checkUserAvailableInDb(userId);

        // Собираем все события принадлежащие пользователю;
        List<Event> events = eventStorage.findEventByInitiator_Id(userId, PageRequest.of(from / size, size))
                .stream()
                .collect(Collectors.toList());

        log.info("Получение пользователем userId={} списка созданных событий: eventsSize={}", user.getId(), events.size());
        return events.stream().map(EventMapper::fromEventToEventFullDto).collect(Collectors.toList());
    }

    /*
    GET EVENT - Получение полной информации о событии добавленном текущим пользователем:
    */
    @Override
    public EventFullDto getEventByIdByCurrentUser(long userId, long eventId) {

        // Проверяем, что пользователь существует;
        User user = userService.checkUserAvailableInDb(userId);

        // Проверяем, что событие существует;
        Event event = checkEventAvailableInDb(eventId);

        // Проверяем что событие принадлежит текущему пользователю;
        checkOwnEvent(event, user);

        log.info("Получение пользователем userId={} своего события: {}", userId, event);
        return EventMapper.fromEventToEventFullDto(event);
    }

    /**
     * PATCH EVENT - Отмена события добавленного текущим пользователем.
     * <p>
     * Обратите внимание:
     * <p>
     * - Отменить можно только событие в состоянии ожидания модерации;
     *
     * @param userId  идентификатор пользователя;
     * @param eventId дентификатор события;
     * @return EventFullDto #{@link EventFullDto}
     */
    @Override
    public EventFullDto cancelEventByCurrentUser(long userId, long eventId) {

        User user = userService.checkUserAvailableInDb(userId);

        Event event = checkEventAvailableInDb(eventId);

        checkOwnEvent(event, user);

        checkStatePending(event);

        // Сетим статус отмены и сохраняем в БД;
        event.setState(StateEnum.CANCELED.toString());
        eventStorage.save(event);

        // Мапим EventFullDto из event;
        EventFullDto result = EventMapper.fromEventToEventFullDto(event);

        log.info("Отмена пользователем userId={} своего события: result={}", userId, result);
        return result;
    }

    /**
     * GET EVENT - Получение информации о запросах на участие в событии текущего пользователя:
     *
     * @param userId  идентификатор пользователя;
     * @param eventId идентификатор события;
     * @return возвращаем коллекцию ParticipationRequestDto #{@link ParticipationRequestDto}
     */
    @Override
    public List<ParticipationRequestDto> getRequestsByEventByCurrentUser(long userId, long eventId) {

        User user = userService.checkUserAvailableInDb(userId);

        Event event = checkEventAvailableInDb(eventId);

        checkOwnEvent(event, user);

        // Находим все запросы на данное событие;
        List<Request> requests = requestStorage.findAllByEvent_Id(eventId);

        // Мапим все найденные запросы в лист ParticipationRequestDto;
        log.info("Получили все запросы на событие: eventId={} созданного пользователем userId={}: requests {}",
                event.getId(), user.getId(), requests.toString());
        return requests.stream()
                .map(RequestMapper::fromRequestToParticipationRequestDto)
                .collect(Collectors.toList());
    }

    /**
     * PATCH EVENT - Подтверждение чужой заявки на участие в событии текущего пользователя.
     * <p>
     * Обратите внимание:
     * <p>
     * - если для события лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется;
     * <p>
     * - нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие;
     * <p>
     * - если при подтверждении данной заявки, лимит заявок для события исчерпан, то все неподтверждённые заявки необходимо отклонить;
     *
     * @param userId  идентификатор пользователя;
     * @param eventId идентификатор события;
     * @param reqId   идентификатор запроса;
     * @return ParticipationRequestDto #{@link ParticipationRequestDto}
     */
    @Override
    public ParticipationRequestDto acceptRequestOnEventByCurrentUser(long userId, long eventId, long reqId) {

        User user = userService.checkUserAvailableInDb(userId);

        Event event = checkEventAvailableInDb(eventId);

        checkOwnEvent(event, user);

        Request request = checkRequestAvailableInDb(reqId);

        ParticipationRequestDto pRDto = RequestMapper.fromRequestToParticipationRequestDto(request);


        // Проверяем лимит заявок для участия в событии и модерацию, если нет лимита и отключена модерация;
        if (checkRequestLimitAndModeration(event)) {
            // Cетим статус и сохраняем обновленные данные в БД;
            request.setStatus("CONFIRMED");
            requestStorage.save(request);

            // Если данный запрос стал последним из одобренных;
        } else {
            // Cетим статус и сохраняем обновленные данные в БД;
            request.setStatus("CONFIRMED");
            requestStorage.save(request);

            // А остальные не одобренные запросы отклоняем;
            List<Request> requests = requestStorage.findAllByEvent_IdAndStatus(eventId, "PENDING");
            requests.forEach(req -> req.setStatus("CANCELED"));
            requestStorage.saveAll(requests);
        }

        pRDto.setStatus("CONFIRMED");

        log.info("Пользователь userId={} принял запрос reqId={} на событие: eventId={}",
                userId, reqId, eventId);
        return pRDto;
    }

    /**
     * PATCH EVENT - Отклонение чужой заявки на участие в событии текущего пользователя.
     *
     * @param userId  идентификатор пользователя;
     * @param eventId идентификатор события;
     * @param reqId   идентификатор запроса;
     * @return ParticipationRequestDto #{@link ParticipationRequestDto}
     */
    @Override
    public ParticipationRequestDto cancelRequestOnEventByCurrentUser(long userId, long eventId, long reqId) {

        User user = userService.checkUserAvailableInDb(userId);

        Event event = checkEventAvailableInDb(eventId);

        checkOwnEvent(event, user);

        Request request = checkRequestAvailableInDb(reqId);

        // Сетим новый статус;
        request.setStatus("REJECTED");
        requestStorage.save(request);

        log.info("Пользователь userId={} отклонил запрос reqId={} на событие: eventId={}",
                userId, reqId, eventId);
        return RequestMapper.fromRequestToParticipationRequestDto(request);
    }

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
     * @param text       ключевые слова введенные пользователем, по которым будем искать события;
     * @param categories коллекция категорий;
     * @param paid       платные события или бесплатные;
     * @param start      дата и время начала событий;
     * @param end        дата и время окончания событий;
     * @param sort       сортировка по дате события (по умолчанию) или по количеству просмотров;
     * @param from       с какой страницы будем начинать просмотр;
     * @param size       сколько событий на страницу будем показывать;
     * @return возвращаем коллекцию из EventShortDto #{@link EventShortDto}
     */
    @Override
    public List<EventShortDto> getAllPublicEvents(String text,
                                                  List<Long> categories,
                                                  Boolean paid,
                                                  LocalDateTime start,
                                                  LocalDateTime end,
                                                  String sort,
                                                  Integer from,
                                                  Integer size) {

        // Собираем все события согласно переданным параметрам;
        List<Event> events = eventStorage.findAllPublicEvents(
                        text,
                        categories,
                        paid,
                        start,
                        end,
                        PageRequest.of(from / size, size))
                .stream().collect(Collectors.toList());
        log.info("Найденные события events={}", events);

        // Проверяем, что данные были найдены;
        if (events.isEmpty()) {
            throw new BadRequestException("По заданным параметрам события не найдены!");
        }

        List<Long> eventsIds = getCurrentEventIds(events);

        // Находим пары id события - просмотры;
        Map<Long, Integer> idViewsPairs = getViewsByEventsId(eventsIds);
        log.info("Найденные idViewsPairs={}", idViewsPairs);

        // Находим пары id события - подтвержденные запросы;
        Map<Long, Long> idConfReqPairs = getAllConfirmedRequests(eventsIds);
        log.info("Найденные idConfReqPairs={}", idConfReqPairs);

        // Создаем результирующий объект и мапим в нужную форму;
        List<EventShortDto> result = events.stream()
                .map(EventMapper::fromEventToEventShortDto)
                .collect(Collectors.toList());

        // Сетим просмотры и подтвержденные запросы каждому событию;
        for (EventShortDto eventShortDto : result) {
            if (idViewsPairs.containsKey(eventShortDto.getId())) {
                eventShortDto.setViews(idViewsPairs.get(eventShortDto.getId()));
                eventShortDto.setConfirmedRequests(idConfReqPairs.get(eventShortDto.getId()));
            }
        }

        log.info("Выводим все публичные события : result={}", result);

        // Результат выводим согласно пришедшему параметру сортировки, по-умолчанию: По дате события;
        if (sort.equals("VIEWS")) {

            return result.stream()
                    .sorted((Comparator.comparing(EventShortDto::getViews)))
                    .collect(Collectors.toList());
        } else {

            log.info("Выводим все публичные события : result={}", result);
            return result.stream().sorted((Comparator.comparing(EventShortDto::getEventDate)))
                    .collect(Collectors.toList());
        }
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
     * @param eventId идентификатор события;
     * @return EventFullDto #{@link EventFullDto}
     */
    @Override
    public EventFullDto getPublicEventById(long eventId) {

        Event event = checkEventAvailableInDb(eventId);

        checkStatusPublished(eventId);

        EventFullDto result = EventMapper.fromEventToEventFullDto(event);

        // Проверяем количество подтвержденных запросов и сетим их в результат;
        result.setConfirmedRequests(getConfirmedRequests(eventId));

        // Находим просмотры события;
        Map<Long, Integer> idViewsPairs = getViewsByEventsId(List.of(eventId));
        log.info("Найденные idViewsPairs={}", idViewsPairs);
        Integer views = idViewsPairs.get(eventId);

        // Находим пары id события - подтвержденные запросы;
        Map<Long, Long> idConfReqPairs = getAllConfirmedRequests(List.of(eventId));
        log.info("Найденные idConfReqPairs={}", idConfReqPairs);
        Long confirmedRequests = idConfReqPairs.get(eventId);

        // Сетим просмотры и подтвержденные запросы;
        result.setViews(views);
        result.setConfirmedRequests(confirmedRequests);

        log.info("Выводим публичное событие: result={}", result);
        return result;
    }

    /**
     * GET EVENT ADMIN - Поиск событий.
     * <p>
     * Эндпоинт возвращает полную информацию обо всех событиях подходящих под переданные условия;
     *
     * @param users      коллекция из пользователей;
     * @param states     коллекция из статусов событий;
     * @param categories коллекция из категорий;
     * @param start      дата и время начала поиска событий;
     * @param end        дата и время окончания поиска событий;
     * @param from       с какой страницы начинаем просмотр;
     * @param size       сколько событий на страницу будем показывать;
     * @return возвращаем коллекцию из EventFullDto #{@link EventFullDto}
     */
    @Override
    public List<EventFullDto> searchEvents(List<Long> users,
                                           List<String> states,
                                           List<Long> categories,
                                           LocalDateTime start,
                                           LocalDateTime end,
                                           Integer from,
                                           Integer size) {

        // Сначала находим список событий по указанным параметрам;
        List<Event> events =
                eventStorage.findEventsByAdminSearch(
                                users,
                                states,
                                categories,
                                start,
                                end,
                                PageRequest.of(from / size, size))
                        .stream().collect(Collectors.toList());

        log.info("Найденные события: events={}", events);

        // Собираем id всех событий;
        List<Long> eventsIds = getCurrentEventIds(events);

        // Находим пары id события - просмотры;
        Map<Long, Integer> idViewsPairs = getViewsByEventsId(eventsIds);
        log.info("Найденные idViewsPairs={}", idViewsPairs);

        // Находим пары id события - подтвержденные запросы;
        Map<Long, Long> idConfReqPairs = getAllConfirmedRequests(eventsIds);
        log.info("Найденные idConfReqPairs={}", idConfReqPairs);

        // Создаем результирующий объект и мапим в нужную форму;
        List<EventFullDto> result = events.stream()
                .map(EventMapper::fromEventToEventFullDto)
                .collect(Collectors.toList());

        // Сетим просмотры и подтвержденные запросы каждому событию;
        for (EventFullDto eventFullDto : result) {
            if (idViewsPairs.containsKey(eventFullDto.getId())) {
                eventFullDto.setViews(idViewsPairs.get(eventFullDto.getId()));
                eventFullDto.setConfirmedRequests(idConfReqPairs.get(eventFullDto.getId()));
            }
        }

        log.info("Результат: result={}", result);
        return result;
    }

    /**
     * PUT EVENT ADMIN - Редактирование события.
     * <p>
     * Редактирование данных любого события администратором. Валидация данных не требуется.
     *
     * @param eventId                 идентификатор события;
     * @param adminUpdateEventRequest #{@link AdminUpdateEventRequest}
     * @return EventFullDto #{@link EventFullDto}
     */
    @Override
    public EventFullDto updateEventByAdmin(long eventId, AdminUpdateEventRequest adminUpdateEventRequest) {

        Event event = checkEventAvailableInDb(eventId);

        // Мапим новые данные;
        EventMapper.fromAdminUpdateEventRequestToEvent(event, adminUpdateEventRequest);

        // Категорию сетим отдельно
        if (adminUpdateEventRequest.getCategory() != null) {
            Category category = categoryService.checkCategory(adminUpdateEventRequest.getCategory());
            event.setCategory(category);
        }

        // Сохраняем обновленные данные в БД;
        eventStorage.save(event);

        log.info("Админ изменил событие eventId={}: updateEvent={}", eventId, event);
        return EventMapper.fromEventToEventFullDto(event);
    }

    /**
     * PUT EVENT ADMIN - Публикация события.
     * <p>
     * Обратите внимание:
     * <p>
     * - дата начала события должна быть не ранее чем за час от даты публикации;
     * <p>
     * - событие должно быть в состоянии ожидания публикации;
     */
    @Override
    public EventFullDto eventPublishByAdmin(long eventId) {

        Event currentEvent = checkEventAvailableInDb(eventId);

        checkStatePending(currentEvent);

        // Проверяем дату начала события и публикации, если все в порядке, то сетим и сохраняем;
        LocalDateTime publishedOn = LocalDateTime.now();
        checkEventStartDate(currentEvent.getEventDate(), publishedOn);
        currentEvent.setPublishedOn(publishedOn);
        currentEvent.setState(StateEnum.PUBLISHED.toString());
        eventStorage.save(currentEvent);

        // Возвращаемый объект;
        EventFullDto result = EventMapper.fromEventToEventFullDto(currentEvent);

        log.info("Админ одобрил событие eventId={} теперь оно опубликовано: eventStatus={}", eventId, currentEvent.getState());
        return result;
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
    @Override
    public EventFullDto eventRejectByAdmin(long eventId) {

        Event currentEvent = checkEventAvailableInDb(eventId);

        checkStatePending(currentEvent);

        // Сетим новые данные и сохраняем в БД;
        currentEvent.setState(StateEnum.CANCELED.toString());
        eventStorage.save(currentEvent);

        EventFullDto result = EventMapper.fromEventToEventFullDto(currentEvent);

        log.info("Админ отклонил событие eventId={} теперь оно отменено eventStatus={}:", eventId, currentEvent.getState());
        return result;
    }

    private Request checkRequestAvailableInDb(long reqId) {

        return requestStorage.findById(reqId).orElseThrow(() -> new NotFoundException(String
                .format("Запрос не найден: reqId=%s", reqId)));
    }

    /**
     * Метод проверяет, что событие находится в БД.
     *
     * @param eventId идентификатор события;
     * @return Event #{@link Event}
     */
    @Override
    public Event checkEventAvailableInDb(long eventId) {

        return eventStorage.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие не найдено: eventId=%s", eventId)));
    }

    /**
     * Метод для проверки инициатора события, что событие принадлежит именно этому пользователю.
     *
     * @param event событие;
     * @param user  пользователь;
     */
    @Override
    public void checkOwnEvent(Event event, User user) {

        if (!event.getInitiator().equals(user)) {
            throw new BadRequestException(String
                    .format("Пользователю userId=%s не принадлежит данное событие eventId=%s", user.getId(), event.getId()));
        }
        log.info("Проверяем инициатора userId={} своего события: eventId={}", user, event);
    }

    /**
     * Метод проверяет статус PENDING у Event (что событие находится на модерации).
     *
     * @param event событие;
     */
    private void checkStatePending(Event event) {

        if (!event.getState().equals(StateEnum.PENDING.toString())) {
            throw new BadRequestException(String
                    .format("Событие имеет статус отличный от модерации state=%s", event.getState()));
        }
        log.info("Проверяем статус у eventStateId={} : state={}", event.getId(), event.getState());
    }

    /**
     * Метод проверяет статус PUBLISHED у Event (что событие опубликовано).
     *
     * @param eventId идентификатор события;
     * @return Event #{@link Event}
     */
    @Override
    public Event checkStatusPublished(long eventId) {

        // Проверяем наличие Evevnt в БД;
        Event event = checkEventAvailableInDb(eventId);

        if (!event.getState().equals(StateEnum.PUBLISHED.toString())) {
            throw new BadRequestException(String
                    .format("Событие должно быть опубликовано: state=%s", event.getState()));
        }
        log.info("Проверяем статус у eventStateId={} : state={}", event.getId(), event.getState());
        return event;
    }

    /**
     * Метод проверяет время начала события и время публикации.
     * <p>
     * Событие не может быть опубликовано, если дата начала менее часа после даты публикации.
     *
     * @param eventDate   дата начала события;
     * @param publishedOn дата публикации события;
     */
    private void checkEventStartDate(LocalDateTime eventDate, LocalDateTime publishedOn) {

        if (!eventDate.isAfter(publishedOn.plusHours(1))) {
            throw new BadRequestException(String
                    .format("Событие не может быть опубликовано, так как дата начала eventDate=%s менее чем через" +
                            " час после даты публикации publishedOn=%s", eventDate, publishedOn));
        }
    }

    /**
     * Метод проверяет, чтобы событие было создано не ранее чем за два часа до начала.
     *
     * @param eventDate дата события;
     */
    private void checkEventCreateDate(LocalDateTime eventDate) {

        log.info("Проверяем дату события: eventDate={}", eventDate);
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException(String.format("Событие не может быть раньше, чем через два часа от текущего момента: eventDate=%s", eventDate));
        }
    }

    /**
     * Метод проверяет количество подтвержденных запросов на участие в событии.
     *
     * @param eventId идентификатор события;
     * @return возвращаем количество подтвержденных запросов;
     */
    private Long getConfirmedRequests(long eventId) {

        // Собираем все подтвержденные запросы на событие;
        Long confirmedRequests = requestStorage.countByEvent_IdAndStatus(eventId, "CONFIRMED");

        log.info("Подтвержденных запросов у события eventId={}: confirmedRequests={}", eventId, confirmedRequests);
        return confirmedRequests;
    }

    /**
     * Метод собирает все подтвержденные запросы на события.
     *
     * @param eventsIds коллекция из идентификаторов событий;
     * @return возвращаем Map(Long - идентификатор события, Long - количество подтвержденных запросов)
     */
    private Map<Long, Long> getAllConfirmedRequests(List<Long> eventsIds) {

        // Собираем все подтвержденные запросы на события;
        List<Request> requests = requestStorage.findAllByEvent_IdInAndStatus(eventsIds, "CONFIRMED");
        log.info("Нашли все подтвержденные запросы на события eventsIds={}: requests={}", eventsIds, requests);

        Map<Long, Long> result;

        // Считаем количество подтвержденных запросов и записываем для каждого события;
        result = requests.stream()
                .collect(Collectors.groupingBy(request -> request.getEvent().getId(), Collectors.counting()));

        log.info("Разложили все подтвержденые запросы по событиям: result={}", result);
        return result;
    }

    /**
     * Метод передает запрос в сервис статистики и возвращает количество просмотров события.
     *
     * @param eventsIds коллекция из идентификаторов событий;
     * @return возвращаем Map(Long - идентификатор события, Integer - количество просмотров события)
     */
    private Map<Long, Integer> getViewsByEventsId(List<Long> eventsIds) {

        // Нужны переменные времени для передачи в сервис статистики;
        LocalDateTime start = LocalDateTime.of(2021, 12, 31, 23, 59, 59);
        LocalDateTime end = LocalDateTime.now();

        // Создаем лист в который будем записывать uri;
        List<String> uris = new ArrayList<>();

        // Записываем uri;
        for (Long eventId : eventsIds) {
            uris.add("/events/" + eventId);
        }

        Map<Long, Integer> result = new HashMap<>();

        // Записываем то, что пришло в ответе по отправленным параметрам;
        ResponseEntity<ViewStatsDto[]> response = statClient.getStats(start, end, uris, false);
        log.info("Отправляем в клиент параметры: start={}, end={}, uris={}, unique={}", start, end, uris, false);

        if (response != null && response.getBody() != null) {

            // Создаем объект из ответа;
            List<ViewStatsDto> viewStatsDtos = Arrays.asList(response.getBody());

            for (ViewStatsDto vsd : viewStatsDtos) {

                String id = String.valueOf(vsd.getUri()).substring(8);
                result.put(Long.parseLong(id), vsd.getHits());
                log.info("Записали новую пару: {}", result.get(Long.parseLong(id)));
            }
            log.info("viewStatsDtos={}", viewStatsDtos);
        }

        log.info("Получаем просмотры события result={}", result);
        return result;
    }

    /**
     * Метод принимает список событий и возвращает список их id.
     *
     * @param events коллекция из событий;
     * @return коллекция из идентификаторов событий;
     */
    private List<Long> getCurrentEventIds(List<Event> events) {

        return events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());
    }

    /**
     * Метод получает все события по пришедшим id;
     *
     * @param ids коллекция из идентификаторов событий.
     * @return коллекция из событий.
     */
    @Override
    public List<Event> getEventsByIds(List<Long> ids) {

        log.info("Получаем все события по ids={}", ids.toString());
        return eventStorage.findEventsByIdIn(ids);
    }

    /**
     * Метод проверяет количество одобренных заявок.
     * <p>
     * Если лимит исчерпан, будет выкинуто исключение.
     *
     * @param event событие;
     * @return Если возвращается true - значит лимит не установлен или свободных мест еще много.
     * <p>
     * Если возвращается false - значит осталось последнее место, которое и было занято.
     */
    @Override
    public Boolean checkRequestLimitAndModeration(Event event) {

        long totalLimit = event.getParticipantLimit();

        // Если нет лимита и отключена модерация;
        if (totalLimit == 0
                && event.getRequestModeration().equals(Boolean.FALSE)) {
            return true;
        }
        // Находим количество всех одобренных заявок;
        Long currentLimit = requestStorage.countByEvent_IdAndStatus(event.getId(), "CONFIRMED");

        // Если осталось последнее место;
        if (totalLimit == currentLimit + 1) {
            return false;
            // Если лимит исчерпан;
        } else if (currentLimit >= totalLimit) {
            throw new BadRequestException(String.format("Лимит заявок на событие превышен: eventId=%s", event.getId()));
        }
        return true;
    }

}
