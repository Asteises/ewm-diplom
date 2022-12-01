package ru.praktikum.statsservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EndpointHitDto {

    @JsonProperty("app")
    private String app;

    @JsonProperty("uri")
    private String uri;

    @JsonProperty("ip")
    private String ip;

    @JsonProperty("timestamp")
    private String created;
}
