package com.example.pastebin.api;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class PasteExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public PasteErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        if (message.isBlank()) {
            message = "invalid request";
        }
        return new PasteErrorResponse(PasteErrorCode.VALIDATION_ERROR, message);
    }

    @ExceptionHandler(PasteNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public PasteErrorResponse handleNotFound(PasteNotFoundException ex) {
        return new PasteErrorResponse(PasteErrorCode.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ReadLimitReachedException.class)
    @ResponseStatus(HttpStatus.GONE)
    public PasteErrorResponse handleReadLimitReached(ReadLimitReachedException ex) {
        return new PasteErrorResponse(PasteErrorCode.READ_LIMIT_REACHED, ex.getMessage());
    }

    private String formatFieldError(FieldError error) {
        String field = error.getField();
        String message = error.getDefaultMessage();
        if (message == null || message.isBlank()) {
            message = "invalid";
        }
        return field + ": " + message;
    }
}
