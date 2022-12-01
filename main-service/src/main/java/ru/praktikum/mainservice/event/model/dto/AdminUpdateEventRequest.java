package ru.praktikum.mainservice.event.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.praktikum.mainservice.location.Location;

/**
 * Информация для редактирования события администратором. Все поля необязательные. Значение полей не валидируется.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateEventRequest {
    @JsonProperty("annotation")
    private String annotation;

    @JsonProperty("category")
    private Long category;

    @JsonProperty("description")
    private String description;

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
