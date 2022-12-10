package ru.praktikum.mainservice.comment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Основное DTO для Комментария.
 * <p>
 * <p>
 * <p>
 * Long id - идентификатор комментария;
 * <p>
 * String text - текст комментария;
 * <p>
 * Long eventId - идентификатор события, на которое оставлен комментарий;
 * <p>
 * Long authorId - идентификатор пользователя, который оставил комментарий;
 * <p>
 * LocalDateTime created - дата и время создания комментария;
 * <p>
 * Long commentId - если комментарий оставлен как ответ на другой комментарий;
 * <p>
 * List<CommentDto> answers - если у данного комментария есть ответы;
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {

    private Long id;

    private String text;

    private Long eventId;

    private Long authorId;

    private LocalDateTime created;

    // Если комментарий оставляется как ответ на другой комментарий;
    private Long commentId;

    // Если на комментарий есть ответы;
    private List<CommentDto> answers;
}
