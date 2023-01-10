package ru.praktikum.mainservice.event.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.praktikum.mainservice.event.mapper.EventMapper;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Класс для валидации даты и времени.
 */
@Slf4j
@Service
public class EventFilterValidDates {

    /**
     * Метод для валидации даты и времени.
     *
     * @param rangeStart начальная дата поиска событий;
     * @param rangeEnd   окончательная дата поиска событий;
     * @return возвращаем Map<String, LocalDateTime>
     */
    public Map<String, LocalDateTime> checkAndFormat(String rangeStart, String rangeEnd) {

        // Создаем переменные для LocalDateTime;
        LocalDateTime start;
        LocalDateTime end;

        // Если параметры пришли пустые, то устанавливаем свои;
        if (rangeStart == null || rangeEnd == null) {

            // Ищем от текущего момента времени;
            start = LocalDateTime.now();

            // Так как в БД лежит TIMESTAMP, то устанавливаем верхнюю границу поиска для него;
            end = LocalDateTime.of(9998, 12, 31, 23, 59, 59);

            // Иначе просто форматируем под нужный формат;
        } else {
            start = LocalDateTime.parse(rangeStart, EventMapper.FORMATTER_EVENT_DATE);
            end = LocalDateTime.parse(rangeEnd, EventMapper.FORMATTER_EVENT_DATE);
        }
        log.info("Даты между которых будем искать события: start={}, end={}", start, end);
        return Map.of("start", start, "end", end);
    }
}
