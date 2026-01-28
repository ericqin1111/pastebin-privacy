package com.example.pastebin.api;

public class ReadLimitReachedException extends RuntimeException {
    public ReadLimitReachedException(String message) {
        super(message);
    }
}
