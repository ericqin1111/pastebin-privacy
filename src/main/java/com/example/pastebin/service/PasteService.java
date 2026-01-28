package com.example.pastebin.service;

import com.example.pastebin.config.PasteProperties;
import com.example.pastebin.model.PasteRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class PasteService {

    private static final Set<String> ALLOWED_TYPES = Set.of("plain", "markdown", "code", "image");

    private final PasteStorageService storageService;
    private final PasteProperties properties;

    public PasteService(PasteStorageService storageService, PasteProperties properties) {
        this.storageService = storageService;
        this.properties = properties;
    }

    public String createPaste(String iv,
                              String ciphertext,
                              String type,
                              String language,
                              int maxReads,
                              long sizeBytes) {
        validateType(type);
        validateSize(sizeBytes);
        validateMaxReads(maxReads);

        String normalizedType = type.toLowerCase(Locale.ROOT);
        String normalizedLanguage = language == null || language.isBlank() ? null : language.trim();
        String id = generateId();

        PasteRecord record = new PasteRecord(
                id,
                iv,
                ciphertext,
                normalizedType,
                normalizedLanguage,
                Instant.now(),
                maxReads,
                0
        );

        storageService.save(record);
        return id;
    }

    private void validateType(String type) {
        if (type == null || type.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "type is required");
        }
        String normalized = type.toLowerCase(Locale.ROOT);
        if (!ALLOWED_TYPES.contains(normalized)) {
            throw new ResponseStatusException(BAD_REQUEST, "unsupported type");
        }
    }

    private void validateSize(long sizeBytes) {
        long maxBytes = properties.getMaxSize() * 1024L * 1024L;
        if (sizeBytes <= 0 || sizeBytes > maxBytes) {
            throw new ResponseStatusException(BAD_REQUEST, "payload too large");
        }
    }

    private void validateMaxReads(int maxReads) {
        if (maxReads <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "maxReads must be positive");
        }
        if (maxReads > properties.getMaxReadsLimit()) {
            throw new ResponseStatusException(BAD_REQUEST, "maxReads exceeds limit");
        }
    }

    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
