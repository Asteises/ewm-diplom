package ru.praktikum.mainservice.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.praktikum.mainservice.client.dto.ViewStatsDto;

import java.util.List;
import java.util.Map;

public class BaseClient {

    protected final RestTemplate rest;

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    public <T> ResponseEntity<Object> post(String path, T body) {

        return makeAndSendRequest(HttpMethod.POST, path, null, body);
    }

    public <T> ResponseEntity<Object> get(String patch, @Nullable Map<String, Object> parameters) {

        return makeAndSendRequest(HttpMethod.GET, patch, parameters, null);
    }

    public ResponseEntity<ViewStatsDto[]> getArray(String patch, @Nullable Map<String, Object> parameters) {

        assert parameters != null;

        return rest.getForEntity(patch, ViewStatsDto[].class, parameters);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(
            HttpMethod method, String path,
            @Nullable Map<String, Object> parameters,
            @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders());

        ResponseEntity<Object> responseEntity;
        try {
            if (parameters != null) {
                responseEntity = rest.exchange(path, method, requestEntity, Object.class, parameters);
            } else {
                responseEntity = rest.exchange(path, method, requestEntity, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        return prepareResponse(responseEntity);
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        return headers;
    }

    private static ResponseEntity<Object> prepareResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());
        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }
        return responseBuilder.build();
    }
}
