package ru.praktikum.statsservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.springframework.stereotype.Service;
import ru.praktikum.statsservice.mapper.StatMapper;
import ru.praktikum.statsservice.model.EndpointHit;
import ru.praktikum.statsservice.model.dto.EndpointHitDto;
import ru.praktikum.statsservice.model.dto.ViewStatsDto;
import ru.praktikum.statsservice.repository.StatStorage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {

    private final StatStorage statStorage;

    public static final DateTimeFormatter FORMATTER_EVENT_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /*
    POST - Сохранение информации о том, что к эндпоинту был запрос
        + Сохранение информации о том, что на uri конкретного сервиса был отправлен запрос пользователем.
        + Название сервиса, uri и ip пользователя указаны в теле запроса.
    */
    @Override
    public void save(EndpointHitDto endpointHitDto) {

        // Создаем EndpointHit, мапим его и сохраняем в БД;
        EndpointHit endpointHit = StatMapper.toEndpointHit(endpointHitDto);

        log.info("Сохранили endpointHit={}", endpointHitDto);
        statStorage.save(endpointHit);
    }

    /*
    GET - Получение статистики по посещениям.
        Обратите внимание:
            значение даты и времени нужно закодировать (например используя java.net.URLEncoder.encode).
     */
    @Override
    public List<ViewStatsDto> getEventsStatInfo(String start, String end, List<String> uris, Boolean unique) {

        // Парсим LocalDateTime из String;
        LocalDateTime currentStart = LocalDateTime.parse(start, FORMATTER_EVENT_DATE);
        LocalDateTime currentEnd = LocalDateTime.parse(end, FORMATTER_EVENT_DATE);

        // Создаем результирующий объект;
        List<ViewStatsDto> result = new ArrayList<>();

        // Промежуточный лист EndpointHit для простоты восприятия;
        List<EndpointHit> endpointHits = new ArrayList<>();

        // Если список uris пришел пустой, то запишем в него все что есть в БД для данного приложения в диапазоне времени;
        if (uris == null) {
            endpointHits = statStorage.findAllByCreatedBetween(currentStart, currentEnd);

            // Так как передавать пустой список uri нельзя, сетим в него все uri которые нашли;
            uris = endpointHits.stream().map(EndpointHit::getUri).collect(Collectors.toList());

        } else {
            // Если список uri пришел не пустой, то находим по нему;
            endpointHits = statStorage.findAllByUriInAndCreatedBetween(uris, currentStart, currentEnd);
        }

        // Если нужны данные с уникальными ip, то сначала обновим лист endpointHits;
        if (unique) {

            // Воспользуемся StreamEx, чтобы оставить в списке только уникальные значения;
            endpointHits = StreamEx.of(endpointHits)
                    .distinct(EndpointHit::getIp)
                    .collect(Collectors.toList());
        }

        // Проходимся по списку uris, чтобы посчитать просмотры для каждого;
        for (String uri : uris) {

            // Создаем ViewStatsDto в который будем записывать данные;
            ViewStatsDto viewStatsDto = new ViewStatsDto();

            // Сетим app и uri;
            viewStatsDto.setApp("main-service");
            viewStatsDto.setUri(uri);

            // Находим количество просмотров по каждому uri и сетим;
            Integer views = Math.toIntExact(endpointHits.stream()
                    .filter(endpointHit -> endpointHit.getUri().equals(uri))
                    .count());
            viewStatsDto.setHits(views);
            log.info("Количество просмотров для uri={}: views={}", uri, views);

            // Добавляем в результат;
            result.add(viewStatsDto);
        }

        log.info("Получаем result={}", result);
        return result;
    }
}
