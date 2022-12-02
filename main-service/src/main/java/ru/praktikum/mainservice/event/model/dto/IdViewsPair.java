package ru.praktikum.mainservice.event.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdViewsPair {

    private Long id;

    private Integer views;
}
