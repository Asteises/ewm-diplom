package ru.praktikum.mainservice.comment.mapper;

import org.springframework.stereotype.Service;
import ru.praktikum.mainservice.comment.model.Comment;
import ru.praktikum.mainservice.comment.model.dto.CommentDto;
import ru.praktikum.mainservice.comment.model.dto.NewCommentDto;

@Service
public class CommentMapper {

    public static Comment toComment(NewCommentDto newCommentDto) {

        return Comment.builder()
                .text(newCommentDto.getText())
                .commentId(newCommentDto.getCommentId())
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {

        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .eventId(comment.getEvent().getId())
                .authorId(comment.getAuthor().getId())
                .commentId(comment.getCommentId())
                .build();
    }
}
