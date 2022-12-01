package ru.praktikum.mainservice.request.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Данные для изменения информации о событии
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventRequest {
    @JsonProperty("annotation")
    private String annotation;

    @JsonProperty("category")
    private Long category;

    @JsonProperty("description")
    private String description;

    @JsonProperty("eventDate")
    private String eventDate;

    @JsonProperty("eventId")
    private Long eventId;

    @JsonProperty("paid")
    private Boolean paid;

    @JsonProperty("participantLimit")
    private Integer participantLimit;

    @JsonProperty("title")
    private String title;
}
