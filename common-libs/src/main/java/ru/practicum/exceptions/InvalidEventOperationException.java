package ru.practicum.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidEventOperationException extends RuntimeException {
    public InvalidEventOperationException(String message) {
        super(message);
    }
}
