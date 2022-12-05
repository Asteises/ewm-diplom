package ru.praktikum.mainservice.location;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Широта и долгота места проведения события
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "locations")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JsonProperty("lat")
    @Column(name = "lat", nullable = false)
    private Float lat;

    @JsonProperty("lon")
    @Column(name = "lon", nullable = false)
    private Float lon;
}
