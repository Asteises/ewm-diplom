package ru.praktikum.mainservice.request.mapper;

import org.springframework.stereotype.Service;
import ru.praktikum.mainservice.request.model.Request;
import ru.praktikum.mainservice.request.model.dto.ParticipationRequestDto;

import java.time.format.DateTimeFormatter;

@Service
public class RequestMapper {

    private static final DateTimeFormatter FORMATTER_EVENT_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ParticipationRequestDto fromRequestToParticipationRequestDto(Request request) {
        ParticipationRequestDto prDto = new ParticipationRequestDto();
        prDto.setId(request.getId());
        prDto.setCreated(request.getCreated().format(FORMATTER_EVENT_DATE));
        prDto.setEvent(request.getEvent().getId());
        prDto.setRequester(request.getRequester().getId());
        prDto.setStatus(request.getStatus());
        return prDto;
    }
}
