package ru.praktikum.mainservice.comment.Mapper;

import org.springframework.stereotype.Service;
import ru.praktikum.mainservice.comment.model.Comment;
import ru.praktikum.mainservice.comment.model.dto.CommentDto;
import ru.praktikum.mainservice.comment.model.dto.NewCommentDto;

import java.time.LocalDateTime;

@Service
public class CommentMapper {

    public static Comment toComment(NewCommentDto newCommentDto) {

        Comment comment = new Comment();

        comment.setText(newCommentDto.getText());
        comment.setCreated(LocalDateTime.now());
        comment.setEvent(null);
        comment.setAuthor(null);

        return comment;
    }

    public static CommentDto toCommentDto(Comment comment) {

        CommentDto commentDto = new CommentDto();

        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setCreated(comment.getCreated());
        commentDto.setEventId(comment.getEvent().getId());
        commentDto.setAuthorId(comment.getAuthor().getId());

        return commentDto;
    }
}
