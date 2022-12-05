package ru.praktikum.mainservice.comment.model.service;

import org.springframework.stereotype.Service;
import ru.praktikum.mainservice.comment.model.dto.CommentDto;
import ru.praktikum.mainservice.comment.model.dto.NewCommentDto;

import java.util.List;

@Service
public interface CommentService {

    CommentDto postComment(NewCommentDto newCommentDto, long userId, long eventId);

    CommentDto getCommentById(long eventId, long commentId);

    List<CommentDto> getAllCommentsByEventId(long eventId);

    CommentDto editComment(NewCommentDto newCommentDto, long userId, long eventId, long commentId);

    void deleteComment(long userId, long eventId, long commentId);
}
