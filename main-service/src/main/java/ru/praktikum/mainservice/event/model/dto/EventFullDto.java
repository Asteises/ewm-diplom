package ru.praktikum.mainservice.event.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.praktikum.mainservice.category.model.dto.CategoryDto;
import ru.praktikum.mainservice.event.enums.StateEnum;
import ru.praktikum.mainservice.location.Location;
import ru.praktikum.mainservice.user.model.dto.UserShortDto;

/**
 * EventFullDto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {
    @JsonProperty("annotation")
    private String annotation;

    @JsonProperty("category")
    private CategoryDto category;

    @JsonProperty("confirmedRequests")
    private Long confirmedRequests;

    @JsonProperty("createdOn")
    private String createdOn;

    @JsonProperty("description")
    private String description;

    @JsonProperty("eventDate")
    private String eventDate;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("initiator")
    private UserShortDto initiator;

    @JsonProperty("location")
    private Location location;

    @JsonProperty("paid")
    private Boolean paid;

    @JsonProperty("participantLimit")
    private Integer participantLimit;

    @JsonProperty("publishedOn")
    private String publishedOn;

    @JsonProperty("requestModeration")
    private Boolean requestModeration;

    @JsonProperty("state")
    private StateEnum state;

    @JsonProperty("title")
    private String title;

    @JsonProperty("views")
    private Integer views;
}
