package ru.praktikum.statsservice.mapper;

import org.springframework.stereotype.Service;
import ru.praktikum.statsservice.model.EndpointHit;
import ru.praktikum.statsservice.model.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class StatMapper {

    public static final DateTimeFormatter FORMATTER_EVENT_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static EndpointHit toEndpointHit(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = new EndpointHit();

        endpointHit.setApp(endpointHitDto.getApp());
        endpointHit.setIp(endpointHitDto.getIp());
        endpointHit.setUri(endpointHitDto.getUri());
        endpointHit.setCreated(LocalDateTime.parse(endpointHitDto.getCreated(), FORMATTER_EVENT_DATE));

        return endpointHit;
    }
}
