package ru.praktikum.mainservice.comment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class NewCommentDto {

    @NotNull
    private String text;
}
