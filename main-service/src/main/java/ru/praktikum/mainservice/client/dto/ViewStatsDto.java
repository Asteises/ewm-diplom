package ru.praktikum.mainservice.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * ViewStats
 */
@Validated
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViewStatsDto {


    @JsonProperty("app")
    private String app;

    @JsonProperty("uri")
    private String uri;

    @JsonProperty("hits")
    private Long hits;
}
