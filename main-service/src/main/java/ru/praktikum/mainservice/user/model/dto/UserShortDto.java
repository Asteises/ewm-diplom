package ru.praktikum.mainservice.user.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * Пользователь (краткая информация)
 */

@Validated
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserShortDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;
}
