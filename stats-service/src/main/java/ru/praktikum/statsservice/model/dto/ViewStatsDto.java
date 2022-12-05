package ru.praktikum.statsservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ViewStats
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViewStatsDto {


    @JsonProperty("app")
    private String app;

    @JsonProperty("uri")
    private String uri;

    @JsonProperty("hits")
    private Integer hits;
}
