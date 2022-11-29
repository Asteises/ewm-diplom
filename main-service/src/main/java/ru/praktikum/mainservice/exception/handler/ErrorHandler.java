package ru.praktikum.mainservice.exception.handler;

import org.hibernate.PropertyValueException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.praktikum.mainservice.exception.ApiError;
import ru.praktikum.mainservice.exception.BadRequestException;
import ru.praktikum.mainservice.exception.ConflictException;
import ru.praktikum.mainservice.exception.NotFoundException;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<String> userNotFoundHandler(final RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler({BadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse badRequestHandler(final RuntimeException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({PropertyValueException.class})
    public ResponseEntity<ApiError> apiErrorPropertyValueHandler(final RuntimeException e) {
        ApiError apiError = new ApiError();
        apiError.setStatus(ApiError.StatusEnum._400_BAD_REQUEST);
        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler({NullPointerException.class})
    public ResponseEntity<ApiError> apiErrorNullPointerHandler(final RuntimeException e) {
        ApiError apiError = new ApiError();
        apiError.setStatus(ApiError.StatusEnum._400_BAD_REQUEST);
        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler({ConflictException.class})
    public ResponseEntity<ApiError.StatusEnum> apiErrorConflictException(final RuntimeException e) {
        ApiError apiError = new ApiError();
        apiError.setStatus(ApiError.StatusEnum._409_CONFLICT);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError.getStatus());
    }
}
