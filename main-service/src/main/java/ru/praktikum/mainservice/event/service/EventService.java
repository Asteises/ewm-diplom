package ru.praktikum.mainservice.event.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import ru.praktikum.mainservice.event.model.Event;
import ru.praktikum.mainservice.event.model.dto.AdminUpdateEventRequest;
import ru.praktikum.mainservice.event.model.dto.EventFullDto;
import ru.praktikum.mainservice.event.model.dto.EventShortDto;
import ru.praktikum.mainservice.event.model.dto.NewEventDto;
import ru.praktikum.mainservice.request.model.dto.ParticipationRequestDto;
import ru.praktikum.mainservice.request.model.dto.UpdateEventRequest;

import java.time.LocalDateTime;
import java.util.List;

@Service
public interface EventService {

    EventFullDto createEvent(long userId, NewEventDto newEventDto);

    EventFullDto updateEventByCurrentUser(long userId, UpdateEventRequest updateEventRequest);

    List<EventFullDto> getAllEventsByCurrentUser(long userId, Integer from, Integer size);

    EventFullDto getEventByIdByCurrentUser(long userId, long eventId);

    EventFullDto cancelEventByCurrentUser(long userId, long eventId);

    List<ParticipationRequestDto> getRequestsByEventByCurrentUser(long userId, long eventId);

    ParticipationRequestDto acceptRequestOnEventByCurrentUser(long userId, long eventId, long reqId);

    ParticipationRequestDto cancelRequestOnEventByCurrentUser(long userId, long eventId, long reqId);

    List<EventShortDto> getAllPublicEvents(String text,
                                           List<Long> categories,
                                           Boolean paid,
                                           LocalDateTime rangeStart,
                                           LocalDateTime rangeEnd,
                                           String sort,
                                           Integer from,
                                           Integer size);

    EventFullDto getPublicEventById(@PathVariable long id);

    Event checkEventAvailableInDb(long eventId);

    Boolean checkRequestLimitAndModeration(Event event);

    List<EventFullDto> searchEvents(List<Long> users,
                                    List<String> states,
                                    List<Long> categories,
                                    LocalDateTime start,
                                    LocalDateTime end,
                                    Integer from,
                                    Integer size);

    List<Event> getEventsByIds(List<Long> ids);

    EventFullDto updateEventByAdmin(long eventId, AdminUpdateEventRequest adminUpdateEventRequest);

    EventFullDto eventPublishByAdmin(long eventId);

    EventFullDto eventRejectByAdmin(long eventId);

    Event checkStatusPublished(long eventId);
}
