package ru.praktikum.mainservice.comment.model.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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
