package ru.praktikum.mainservice.comment.service;

import org.springframework.stereotype.Service;
import ru.praktikum.mainservice.comment.model.dto.CommentDto;
import ru.praktikum.mainservice.comment.model.dto.EditCommentDto;
import ru.praktikum.mainservice.comment.model.dto.NewCommentDto;

import java.util.List;

@Service
public interface CommentService {

    CommentDto postComment(NewCommentDto newCommentDto, long userId, long eventId);

    CommentDto getCommentById(long eventId, long commentId);

    List<CommentDto> getAllCommentsByEventId(long eventId);

    CommentDto editComment(EditCommentDto editCommentDto, long userId, long eventId, long commentId);

    void deleteCommentByUser(long userId, long eventId, long commentId);

    void deleteCommentByAdmin(long eventId, long commentId);
}
