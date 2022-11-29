package ru.praktikum.mainservice.exception;

//404
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
