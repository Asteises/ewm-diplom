package ru.praktikum.mainservice.comment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.praktikum.mainservice.event.model.Event;
import ru.praktikum.mainservice.user.model.User;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * Основное Entity - Комментарий.
 * <p>
 * <p>
 * <p>
 * Long id - идентификатор;
 * <p>
 * String text - текст комментария;
 * <p>
 * Event event - событие на которое оставлен комментарий;
 * <p>
 * User author - пользователь, автор комментария;
 * <p>
 * LocalDateTime created - дата и время создания комментария;
 * <p>
 * Long commentId - идентификатор комментария, на который был оставлен данный комментарий;
 * <p>
 * Boolean visible - видимость комментария для пользователей;
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "text")
    private String text;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "comment_id")
    private Long commentId;

    @Column(name = "visible")
    private Boolean visible;
}
