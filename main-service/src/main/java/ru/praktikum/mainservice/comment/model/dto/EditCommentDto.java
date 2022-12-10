package ru.praktikum.mainservice.comment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * DTO для изменения комментария пользователем.
 * <p>
 * Можно изменить только текст комментария.
 * <p>
 * String text - новый текст комментария.
 */
@Getter
@Setter
@AllArgsConstructor
public class EditCommentDto {

    @NotNull
    @NotBlank
    private String text;
}
