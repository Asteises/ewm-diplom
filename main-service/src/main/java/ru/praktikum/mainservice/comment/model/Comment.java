package ru.praktikum.mainservice.comment.model;

import lombok.*;
import ru.praktikum.mainservice.event.model.Event;
import ru.praktikum.mainservice.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

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
