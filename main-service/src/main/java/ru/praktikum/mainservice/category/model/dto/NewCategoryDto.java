package ru.praktikum.mainservice.category.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Данные для добавления новой категории
 */

@Validated
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewCategoryDto {

    @JsonProperty("name")
    @NotNull
    @NotBlank
    private String name;
}
