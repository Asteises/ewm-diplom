package ru.praktikum.mainservice.request.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Заявка(запрос) на участие в событии.
 * <p>
 * <p>
 * <p>
 * Long id - идентификатор;
 * <p>
 * String created - дата создания заявки;
 * <p>
 * Long event - идентификатор события;
 * <p>
 * Long requester - идентификатор пользователя оставившего запрос;
 * <p>
 * String status - статус запроса;
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {
    @JsonProperty("created")
    private String created;

    @JsonProperty("event")
    private Long event;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("requester")
    private Long requester;

    @JsonProperty("status")
    private String status;
}
