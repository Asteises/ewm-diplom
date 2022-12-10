package ru.praktikum.mainservice.event.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.praktikum.mainservice.location.Location;

/**
 * Основное DTO - новое событие.
 * <p>
 * <p>
 * <p>
 * String annotation - краткое описание события;
 * <p>
 * Long category - категория события;
 * <p>
 * String description - подробное описание события;
 * <p>
 * String eventDate - дата и время начала события;
 * <p>
 * Location location - место проведения события;
 * <p>
 * Boolean paid - будет событие платным или нет;
 * <p>
 * Integer participantLimit - максимальное количество участников;
 * <p>
 * Boolean requestModeration - будет событие модерироваться администратором или нет;
 * <p>
 * String title - заголовок события;
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {
    @JsonProperty("annotation")
    private String annotation;

    @JsonProperty("category")
    private Long category;

    @JsonProperty("description")
    private String description;

    //дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента
    @JsonProperty("eventDate")
    private String eventDate;

    @JsonProperty("location")
    private Location location;

    @JsonProperty("paid")
    private Boolean paid;

    @JsonProperty("participantLimit")
    private Integer participantLimit;

    @JsonProperty("requestModeration")
    private Boolean requestModeration;

    @JsonProperty("title")
    private String title;
}
