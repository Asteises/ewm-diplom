package ru.praktikum.mainservice.request.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Данные нового пользователя
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {
    @JsonProperty("email")
    private String email;

    @JsonProperty("name")
    private String name;
}
