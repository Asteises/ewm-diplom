package ru.praktikum.mainservice.comment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.praktikum.mainservice.comment.mapper.CommentMapper;
import ru.praktikum.mainservice.comment.model.Comment;
import ru.praktikum.mainservice.comment.model.dto.CommentDto;
import ru.praktikum.mainservice.comment.model.dto.NewCommentDto;
import ru.praktikum.mainservice.comment.repository.CommentStorage;
import ru.praktikum.mainservice.event.model.Event;
import ru.praktikum.mainservice.event.service.EventService;
import ru.praktikum.mainservice.exception.BadRequestException;
import ru.praktikum.mainservice.exception.NotFoundException;
import ru.praktikum.mainservice.user.model.User;
import ru.praktikum.mainservice.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentStorage commentStorage;
    private final UserService userService;
    private final EventService eventService;

    /*
    POST COMMENT - Метод создания и сохранения нового комментария от пользователя.
        + Событие должно быть опубликовано;

     */
    @Override
    public CommentDto postComment(NewCommentDto newCommentDto, long userId, long eventId) {

        // Проверяем, что пользователь существует;
        User author = userService.checkUserAvailableInDb(userId);

        // Проверяем, что событие существует;
        Event event = eventService.checkEventAvailableInDb(eventId);

        // Проверяем, что событие опубликовано;
        eventService.checkStatusPublished(eventId);

        // Создаем новый комментарий, сетим в него данные и сохраняем в БД;
        Comment comment = CommentMapper.toComment(newCommentDto);
        comment.setEvent(event);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        // Устанавливаем видимость комментария;
        comment.setVisible(true);

        // Если комментарий оставлен на другой комментарий, то сетим и его id;
        comment.setCommentId(newCommentDto.getCommentId());

        commentStorage.save(comment);

        // Создаем результирующий объект и мапив в него данные;
        CommentDto result = CommentMapper.toCommentDto(comment);

        log.info("Сохранили новый комментарий: result={}", result);
        return result;
    }

    /*
    GET COMMENT - Получить комментарий по id;
        + комментарии доступны для всех статусов событий;
        + комментарий должен быть видимым;
    */
    @Override
    public CommentDto getCommentById(long eventId, long commentId) {

        // Проверяем, что событие существует;
        Event event = eventService.checkEventAvailableInDb(eventId);

        // Проверяем, что комментарий существует и виден;
        Comment comment = checkCommentById(commentId);

        // Создаем результирующий объект и мапив в него данные;
        CommentDto result = CommentMapper.toCommentDto(comment);

        // Находим в БД видимые ответы на данный комментарий;
        List<Comment> answers = commentStorage.findAllByCommentIdAndVisibleTrue(commentId);

        // Если ответы есть, то сетим в результат;
        if (answers != null) {
            result.setAnswers(answers
                    .stream()
                    .map(CommentMapper::toCommentDto)
                    .collect(Collectors.toList()));
        }

        log.info("Получаем комментарий на событие eventId={}: result={}", event.getId(), result);
        return result;
    }

    /*
    GET COMMENT - Получить все комментарии по eventId;
        + комментарии доступны для всех статусов событий;
        + комментарии должны быть видимыми;
    */
    @Override
    public List<CommentDto> getAllCommentsByEventId(long eventId) {

        // Проверяем, что событие существует;
        Event event = eventService.checkEventAvailableInDb(eventId);

        // Получаем все видимые комментарии на событие;
        List<Comment> comments = commentStorage.findAllByEvent_IdAndVisibleTrue(event.getId());

        // Создаем результирующий объект и мапим в него все комментарии;
        List<CommentDto> result = comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        // Сетим ответы к комментариям;
        for (CommentDto commentDto: result) {
            commentDto.setAnswers(result
                    .stream()
                    .filter(c -> c.getCommentId().equals(commentDto.getId()))
                    .collect(Collectors.toList()));
        }

        log.info("Получаем все комментарии: result={}", result);
        return result;
    }

    /*
    PUT COMMENT - Пользователь вносит изменение в комментарий на событие.
        + комментарий должен принадлежать пользователю;
        + комментарий должен принадлежать событию;
        + комментарий должен быть видимым;
    */
    @Override
    public CommentDto editComment(NewCommentDto newCommentDto, long userId, long eventId, long commentId) {

        // Проверять наличие события и пользователя не требуется, так как эти данные хранятся в комментарии.
        // А комментарий не может существовать, если не существует событие к которому он оставлен
        // или пользователь который его написал. Так как в БД мы удаляем комментарии каскадом.

        // Проверяем, что комментарий существует и видимый;
        Comment comment = checkCommentById(commentId);

        // Проверяем, что комментарий принадлежит данному событию;
        checkCommentOwnEvent(comment, eventId);

        // Проверяем, что комментарий принадлежит данному пользователю;
        checkCommentOwnUser(comment, userId);

        // Сетим новые данные и обьновляем БД;
        comment.setText(newCommentDto.getText());
        commentStorage.save(comment);

        CommentDto result = CommentMapper.toCommentDto(comment);

        log.info("Комментарий изменен: result={}", result);
        return result;
    }

    /*
    DELETE COMMENT - Пользователь удаляет свой комментарий на событие.
        + комментарий должен принадлежать пользователю;
        + комментарий должен принадлежать событию;
        + комментарий должен быть видимым;
    */
    @Override
    public void deleteComment(long userId, long eventId, long commentId) {

        // Проверяем, что комментарий существует;
        Comment comment = checkCommentById(commentId);

        // Проверяем, что комментарий принадлежит данному событию;
        checkCommentOwnEvent(comment, eventId);

        // Проверяем, что комментарий принадлежит данному пользователю;
        checkCommentOwnUser(comment, userId);

        // Проверяем видимость комментария;
        if (!comment.getVisible()) {
            throw new BadRequestException(String.format("Комментарий уже отключен comment=%s", comment));
        }
        // Меняем видимость комментария и обновляем в БД;
        comment.setVisible(false);
        commentStorage.save(comment);

        log.info("Меняем видимость комментария на false: visible={}", comment.getVisible());
    }

    /*
    Метод проверяет наличие комментария в БД и что он видимый;
     */
    private Comment checkCommentById(long commentId) {

        return commentStorage.findByIdAndVisibleTrue(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Комментарий commentId=%s не найден.", commentId)));
    }

    /*
    Метод проверяет, что комментарий принадлежит данному пользователю;
     */
    private void checkCommentOwnUser(Comment comment, long userId) {
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new BadRequestException(String.format("Комментарий comment=%s не принадлежит данному пользователю userId=%s", comment, userId));
        }
    }

    private void checkCommentOwnEvent(Comment comment, long eventId) {
        if (!comment.getEvent().getId().equals(eventId)) {
            throw new BadRequestException(String.format("Комментарий comment=%s не принадлежит данному событию eventId=%s", comment, eventId));
        }
    }
}
