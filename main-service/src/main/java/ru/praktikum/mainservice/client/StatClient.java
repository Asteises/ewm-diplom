package ru.praktikum.mainservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.praktikum.mainservice.client.dto.EndpointHitDto;
import ru.praktikum.mainservice.event.mapper.EventMapper;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class StatClient extends BaseClient {

    private final String getStatUrl = "/stats?start={start}&end={end}&uris={uris}&unique={unique}";
    private final String docker = "${ewm_stats_service_url}";
    private final String localhost = "http://localhost:9090";

    @Autowired
    public StatClient(@Value(docker) String url, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(url))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public void saveRequestInfo(HttpServletRequest httpServletRequest) {

        // Создаем body для запроса и сетим в него данные;
        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setApp("main-service");
        endpointHitDto.setUri(httpServletRequest.getRequestURI());
        endpointHitDto.setIp(httpServletRequest.getRemoteAddr());
        endpointHitDto.setTimestamp(LocalDateTime.now().format(EventMapper.FORMATTER_EVENT_DATE));

        // Передаем запрос в сервис статистики;
        post("/hit", endpointHitDto);
        log.info("Передаем в сервис статистики: endpointHitDto={}", endpointHitDto);
    }

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {

        // Создаем Map для передачи параметров;
        Map<String, Object> parameters = Map.of(
                "start", start.format(EventMapper.FORMATTER_EVENT_DATE),
                "end", end.format(EventMapper.FORMATTER_EVENT_DATE),
                "uris", uris.get(0),
                "unique", unique
        );

        log.info("Из EventController пришел запрос с параметрами: parameters={}", parameters);

        // Направляем запрос с параметрами в сервис статистики, чтобы вернулся ответ ResponseEntity<Object>;
        ResponseEntity<Object> response = get(getStatUrl, parameters);

        log.info("Ответ от сервиса статистики: response={}", response);
        return response;
    }

}
