package com.example.pastebin.model;

import java.time.Instant;

public record PasteRecord(
        String id,
        String iv,
        String ciphertext,
        String type,
        String language,
        Instant createdAt,
        int maxReads,
        int readCount
) {
}
