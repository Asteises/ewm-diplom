package ru.praktikum.mainservice.compilations.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;

/**
 * Подборка событий
 */

@Validated
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {
    @JsonProperty("events")
    @Valid
    private List<Long> events;

    @JsonProperty("pinned")
    private Boolean pinned;

    @JsonProperty("title")
    private String title;
}
