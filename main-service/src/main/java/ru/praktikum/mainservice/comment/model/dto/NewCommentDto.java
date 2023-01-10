package ru.praktikum.mainservice.comment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * DTO для нового комментария от пользователя.
 * <p>
 * Комментарий можно оставить как на само событие, так и на другой комментарий.
 * <p>
 * <p>
 * <p>
 * String text - текст комментария;
 * <p>
 * Long commentId - идентификатор комментария на который хотим оставить ответ;
 */
@Getter
@Setter
@AllArgsConstructor
public class NewCommentDto {

    @NotNull
    @NotBlank
    private String text;

    private Long commentId;
}
