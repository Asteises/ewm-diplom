package ru.praktikum.mainservice.exception;

//400
public class BadRequestException extends RuntimeException {

    public BadRequestException(String error) {
        super(error);
    }
}
