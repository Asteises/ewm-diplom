package ru.praktikum.mainservice.event.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.praktikum.mainservice.category.mapper.CategoryMapper;
import ru.praktikum.mainservice.event.enums.StateEnum;
import ru.praktikum.mainservice.event.model.Event;
import ru.praktikum.mainservice.event.model.dto.AdminUpdateEventRequest;
import ru.praktikum.mainservice.event.model.dto.EventFullDto;
import ru.praktikum.mainservice.event.model.dto.EventShortDto;
import ru.praktikum.mainservice.event.model.dto.NewEventDto;
import ru.praktikum.mainservice.request.model.dto.UpdateEventRequest;
import ru.praktikum.mainservice.user.mapper.UserMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class EventMapper {

    public static final DateTimeFormatter FORMATTER_EVENT_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Event toEvent(NewEventDto newEventDto) {
        Event event = new Event();
        // id - генерируется БД;

        // Для нового события проставляем статус PENDING;
        event.setState(StateEnum.PENDING.toString());

        //Приходит "eventDate": "2024-12-31 15:10:05" - форматируем чтобы получить LocalDateTime;
        LocalDateTime eventDate = LocalDateTime.parse(newEventDto.getEventDate(), FORMATTER_EVENT_DATE);
        event.setEventDate(eventDate);

        event.setTitle(newEventDto.getTitle());
        event.setAnnotation(newEventDto.getAnnotation());
        event.setDescription(newEventDto.getDescription());
        event.setRequestModeration(newEventDto.getRequestModeration());
        event.setPaid(newEventDto.getPaid());
        event.setCreatedOn(LocalDateTime.now());
        event.setLocation(newEventDto.getLocation());
        event.setParticipantLimit(newEventDto.getParticipantLimit().longValue());

        // Категорию проставляем в сервисе;
        event.setCategory(null);

        // Пользователь (инициатор) - приходит в контроллере, проверяем его и сетим в сервисе;
        event.setInitiator(null);

        // Дата публикации проставляется после публикации события админом;
        event.setPublishedOn(null);

        log.info("Мапим NewEventDto в Event: {}", event);
        return event;
    }

    public static EventFullDto fromEventToEventFullDto(Event event) {
        EventFullDto eventFullDto = new EventFullDto();

        eventFullDto.setId(event.getId());
        eventFullDto.setState(StateEnum.fromValue(event.getState()));
        eventFullDto.setTitle(event.getTitle());
        eventFullDto.setAnnotation(event.getAnnotation());
        eventFullDto.setCategory(CategoryMapper.categoryToCategoryDto(event.getCategory()));
        eventFullDto.setPaid(event.getPaid());
        eventFullDto.setEventDate(event.getEventDate().format(FORMATTER_EVENT_DATE));
        eventFullDto.setInitiator(UserMapper.userToUserShortDto(event.getInitiator()));
        eventFullDto.setDescription(event.getDescription());
        eventFullDto.setParticipantLimit(event.getParticipantLimit().intValue());

        eventFullDto.setLocation(event.getLocation());
        eventFullDto.setRequestModeration(event.getRequestModeration());

        log.info("Мапим Event в EventFullDto: {}", eventFullDto);
        return eventFullDto;
    }

    public static EventShortDto fromEventToEventShortDto(Event event) {
        EventShortDto eventShortDto = new EventShortDto();

        eventShortDto.setId(event.getId());
        eventShortDto.setAnnotation(event.getAnnotation());
        eventShortDto.setTitle(event.getTitle());
        eventShortDto.setPaid(event.getPaid());
        eventShortDto.setCategory(CategoryMapper.categoryToCategoryDto(event.getCategory()));
        eventShortDto.setInitiator(UserMapper.userToUserShortDto(event.getInitiator()));
        eventShortDto.setEventDate(event.getEventDate().format(FORMATTER_EVENT_DATE));

        eventShortDto.setConfirmedRequests(null);
        eventShortDto.setViews(null);

        log.info("Мапим Event в EventShortDto: {}", eventShortDto);
        return eventShortDto;
    }

    public static Event fromUpdateEventRequestToEvent(Event event, UpdateEventRequest updateEventRequest) {

        if (updateEventRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventRequest.getAnnotation());
        }
        if (updateEventRequest.getDescription() != null) {
            event.setDescription(updateEventRequest.getDescription());
        }
        if (updateEventRequest.getEventDate() != null) {
            LocalDateTime eventDate = LocalDateTime.parse(updateEventRequest.getEventDate(), FORMATTER_EVENT_DATE);
            event.setEventDate(eventDate);
        }
        if (updateEventRequest.getPaid() != null) {
            event.setPaid(updateEventRequest.getPaid());
        }
        if (updateEventRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventRequest.getParticipantLimit().longValue());
        }
        if (updateEventRequest.getTitle() != null) {
            event.setTitle(updateEventRequest.getTitle());
        }
        return event;
    }

    public static void fromAdminUpdateEventRequestToEvent(Event event, AdminUpdateEventRequest adminUpdateEventRequest) {

        if (adminUpdateEventRequest.getAnnotation() != null) {
            event.setAnnotation(adminUpdateEventRequest.getAnnotation());
        }
        if (adminUpdateEventRequest.getDescription() != null) {
            event.setDescription(adminUpdateEventRequest.getDescription());
        }
        if (adminUpdateEventRequest.getEventDate() != null) {
            LocalDateTime eventDate = LocalDateTime.parse(adminUpdateEventRequest.getEventDate(), FORMATTER_EVENT_DATE);
            event.setEventDate(eventDate);
        }
        if (adminUpdateEventRequest.getLocation() != null) {
            event.setLocation(adminUpdateEventRequest.getLocation());
        }
        if (adminUpdateEventRequest.getPaid() != null) {
            event.setPaid(adminUpdateEventRequest.getPaid());
        }
        if (adminUpdateEventRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(adminUpdateEventRequest.getParticipantLimit().longValue());
        }
        if (adminUpdateEventRequest.getRequestModeration() != null) {
            event.setRequestModeration(adminUpdateEventRequest.getRequestModeration());
        }
        if (adminUpdateEventRequest.getTitle() != null) {
            event.setTitle(adminUpdateEventRequest.getTitle());
        }
    }

    public static EventShortDto fromFullDtoToShortDto(EventFullDto eventFullDto) {
        EventShortDto eventShortDto = new EventShortDto();

        eventShortDto.setId(eventFullDto.getId());
        eventShortDto.setAnnotation(eventFullDto.getAnnotation());
        eventShortDto.setCategory(eventFullDto.getCategory());
        eventShortDto.setConfirmedRequests(eventFullDto.getConfirmedRequests());
        eventShortDto.setEventDate(eventFullDto.getEventDate());
        eventShortDto.setInitiator(eventFullDto.getInitiator());
        eventShortDto.setPaid(eventFullDto.getPaid());
        eventShortDto.setTitle(eventFullDto.getTitle());
        eventShortDto.setViews(eventFullDto.getViews());

        return eventShortDto;
    }
}
