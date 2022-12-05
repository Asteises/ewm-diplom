package ru.praktikum.mainservice.request.service;

import org.springframework.stereotype.Service;
import ru.praktikum.mainservice.request.model.Request;
import ru.praktikum.mainservice.request.model.dto.ParticipationRequestDto;

import java.util.List;

@Service
public interface RequestService {

    ParticipationRequestDto createRequest(long userId, long eventId);

    ParticipationRequestDto cancelOwnRequest(long userId, long requestId);

    List<ParticipationRequestDto> getRequests(long userId);

    Request checkRequestAvailableInDb(long requestId);
}
