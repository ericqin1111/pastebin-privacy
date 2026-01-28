package com.example.pastebin.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PasteExceptionHandler {

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
}
