package com.example.pastebin.api;

public record PasteErrorResponse(
        PasteErrorCode code,
        String message
) {
}
