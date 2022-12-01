package ru.praktikum.mainservice.event.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Широта и долгота места проведения события
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @JsonProperty("lat")
    private Float lat;

    @JsonProperty("lon")
    private Float lon;
}
