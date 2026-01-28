package com.example.pastebin.api;

public record GetPasteResponse(
        String id,
        String iv,
        String ciphertext,
        String type,
        String language,
        int maxReads,
        int readCount
) {
}
