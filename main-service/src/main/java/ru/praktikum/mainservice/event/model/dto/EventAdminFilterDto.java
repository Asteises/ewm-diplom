package ru.praktikum.mainservice.event.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventAdminFilterDto {

    private Long[] users;
    private String[] states;
    private Long[] categories;
    private String rangeStart;
    private String rangeEnd;
    private Integer from;
    private Integer size;
}
