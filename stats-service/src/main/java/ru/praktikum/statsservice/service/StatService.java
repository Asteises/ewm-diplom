package ru.praktikum.statsservice.service;

import org.springframework.stereotype.Service;
import ru.praktikum.statsservice.model.dto.EndpointHitDto;
import ru.praktikum.statsservice.model.dto.ViewStatsDto;

import java.util.List;

@Service
public interface StatService {

    void save(EndpointHitDto endpointHitDto);

    List<ViewStatsDto> getEventsStatInfo(String start, String end, List<String> uris, Boolean unique);
}
