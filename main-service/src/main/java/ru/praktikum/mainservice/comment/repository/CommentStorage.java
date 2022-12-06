package ru.praktikum.mainservice.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.praktikum.mainservice.comment.model.Comment;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentStorage extends JpaRepository<Comment, Long> {

    List<Comment> findAllByEvent_IdAndVisibleTrue(long eventId);

    List<Comment> findAllByCommentIdAndVisibleTrue(long commentId);

    Optional<Comment> findByIdAndVisibleTrue(long eventId);
}
