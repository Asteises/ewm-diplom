package ru.praktikum.statsservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * EndpointHit
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "endpoint_hit")
public class EndpointHit {

    @JsonProperty("id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;

    @JsonProperty("app")
    @Column(name = "app")
    private String app;

    @JsonProperty("uri")
    @Column(name = "uri")
    private String uri;

    @JsonProperty("ip")
    @Column(name = "ip")
    private String ip;

    @JsonProperty("created")
    @Column(name = "created")
    private LocalDateTime created;
}
