package ru.praktikum.mainservice.category.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Категория
 */

@Validated
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    @NotNull
    @NotBlank
    private String name;
}
