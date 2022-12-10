package ru.praktikum.mainservice.comment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.praktikum.mainservice.comment.mapper.CommentMapper;
import ru.praktikum.mainservice.comment.model.Comment;
import ru.praktikum.mainservice.comment.model.dto.CommentDto;
import ru.praktikum.mainservice.comment.model.dto.EditCommentDto;
import ru.praktikum.mainservice.comment.model.dto.NewCommentDto;
import ru.praktikum.mainservice.comment.repository.CommentStorage;
import ru.praktikum.mainservice.event.model.Event;
import ru.praktikum.mainservice.event.service.EventService;
import ru.praktikum.mainservice.exception.BadRequestException;
import ru.praktikum.mainservice.exception.NotFoundException;
import ru.praktikum.mainservice.user.model.User;
import ru.praktikum.mainservice.user.service.UserService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentStorage commentStorage;
    private final UserService userService;
    private final EventService eventService;

    /**
     * POST COMMENT - Метод создания и сохранения нового комментария от пользователя.
     * - Событие должно быть опубликовано;
     *
     * @param newCommentDto #{@link NewCommentDto}
     * @param userId        идентификатор пользователя;
     * @param eventId       идентификатор события;
     * @return CommentDto #{@link CommentDto}
     */
    @Override
    public CommentDto postComment(NewCommentDto newCommentDto, long userId, long eventId) {

        User author = userService.checkUserAvailableInDb(userId);

        Event event = eventService.checkEventAvailableInDb(eventId);

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

    /**
     * GET COMMENT - Получить комментарий по id.
     * <p>
     * - комментарии доступны для всех статусов событий;
     * <p>
     * - комментарий должен быть видимым;
     *
     * @param eventId   идентификатор события;
     * @param commentId идентификатор комментария;
     * @return CommentDto #{@link CommentDto}
     */
    @Override
    public CommentDto getCommentById(long eventId, long commentId) {

        Event event = eventService.checkEventAvailableInDb(eventId);

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

    /**
     * GET COMMENT - Получить все комментарии по eventId.
     * <p>
     * - комментарии доступны для всех статусов событий;
     * <p>
     * - комментарии должны быть видимыми;
     *
     * @param eventId идентификатор события;
     * @return возвращаем коллекцию из #{@link CommentDto}
     */
    @Override
    public List<CommentDto> getAllCommentsByEventId(long eventId) {

        Event event = eventService.checkEventAvailableInDb(eventId);

        List<Comment> comments = commentStorage.findAllByEvent_IdAndVisibleTrue(event.getId());
        log.info("Все комментарии на событие: {}", comments);

        // Создаем результирующий объект и мапим в него все комментарии;
        List<CommentDto> result = comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        // Сетим ответы к комментариям;
        for (CommentDto commentDto : result) {
//            commentDto.setAnswers(result
//                    .stream()
//                    .filter(c -> c.getCommentId().equals(commentDto.getId()))
//                    .collect(Collectors.toList()));
        }

        log.info("Получаем все комментарии: result={}", result);
        return result;
    }

    /**
     * PATCH COMMENT - Пользователь вносит изменение в комментарий на событие.
     * <p>
     * - комментарий должен принадлежать пользователю;
     * <p>
     * - комментарий должен принадлежать событию;
     * <p>
     * - проверять наличие события и пользователя не требуется, так как эти данные хранятся в комментарии.
     * А комментарий не может существовать, если не существует событие и пользователь который его написал.
     *
     * @param editCommentDto #{@link EditCommentDto}
     * @param userId         идентификатор пользователя;
     * @param eventId        идентификатор события;
     * @param commentId      идентификатор комментария;
     * @return CommentDto #{@link CommentDto}
     */
    @Override
    public CommentDto editComment(EditCommentDto editCommentDto, long userId, long eventId, long commentId) {

        Comment comment = checkCommentById(commentId);

        checkCommentOwnEvent(comment, eventId);

        checkCommentOwnUser(comment, userId);

        // Сетим новые данные и обьновляем БД;
        comment.setText(editCommentDto.getText());
        commentStorage.save(comment);

        CommentDto result = CommentMapper.toCommentDto(comment);

        log.info("Комментарий изменен: result={}", result);
        return result;
    }

    /**
     * DELETE COMMENT - Пользователь удаляет свой комментарий на событие.
     * <p>
     * Обратите внимание:
     * <p>
     * - комментарий должен принадлежать пользователю;
     * <p>
     * - комментарий должен принадлежать событию;
     * <p>
     * - комментарий должен быть видимым;
     *
     * @param userId    идентификатор пользователя;
     * @param eventId   идентификатор события;
     * @param commentId идентификатор комментария;
     */
    @Override
    public void deleteCommentByUser(long userId, long eventId, long commentId) {

        Comment comment = checkCommentById(commentId);

        checkCommentOwnEvent(comment, eventId);

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

    /**
     * DELETE COMMENT BY ADMIN - администратор меняет видимость комментария на false.
     *
     * @param eventId   идентификатор события;
     * @param commentId идентификатор комментария;
     */
    @Override
    public void deleteCommentByAdmin(long eventId, long commentId) {

        Comment comment = checkCommentById(commentId);

        checkCommentOwnEvent(comment, eventId);

        if (!comment.getVisible()) {
            throw new BadRequestException(String.format("Комментарий уже отключен comment=%s", comment));
        }
        // Меняем видимость комментария и обновляем в БД;
        comment.setVisible(false);
        commentStorage.save(comment);

        log.info("Меняем видимость комментария на false: visible={}", comment.getVisible());
    }

    /**
     * Метод проверяет наличие комментария в БД и что он видимый.
     *
     * @param commentId идентификатор комментария;
     * @return Comment #{@link Comment}
     */
    private Comment checkCommentById(long commentId) {

        return commentStorage.findByIdAndVisibleTrue(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Комментарий commentId=%s не найден.", commentId)));
    }

    /**
     * Метод проверяет, что комментарий принадлежит данному пользователю.
     *
     * @param comment #{@link Comment}
     * @param userId  идентификатор пользователя;
     */
    private void checkCommentOwnUser(Comment comment, long userId) {
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new BadRequestException(String.format("Комментарий comment=%s не принадлежит данному пользователю userId=%s", comment, userId));
        }
    }

    /**
     * Метод проверяет, принадлежит ли комментарий данному событию.
     *
     * @param comment #{@link Comment}
     * @param eventId идентификатор события;
     */
    private void checkCommentOwnEvent(Comment comment, long eventId) {
        if (!comment.getEvent().getId().equals(eventId)) {
            throw new BadRequestException(String.format("Комментарий comment=%s не принадлежит данному событию eventId=%s", comment, eventId));
        }
    }

    private Map<Long, Long> checkAnswersForCommentsByEvent(List<Comment> comments) {
        Map<Long, Long> commentPairs = new HashMap<>();
        for (Comment comment : comments) {
            if (comment.getCommentId() != null) {
                commentPairs.put(comment.getCommentId(), comment.getId());
            }
        }
        return commentPairs;
    }
}
