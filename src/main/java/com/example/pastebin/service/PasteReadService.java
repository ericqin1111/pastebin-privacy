package com.example.pastebin.service;

import com.example.pastebin.api.PasteNotFoundException;
import com.example.pastebin.api.ReadLimitReachedException;
import com.example.pastebin.model.PasteRecord;
import org.springframework.stereotype.Service;

@Service
public class PasteReadService {

    private final PasteStorageService storageService;

    public PasteReadService(PasteStorageService storageService) {
        this.storageService = storageService;
    }

    public PasteReadResult read(String id) {
        PasteRecord record = storageService.findById(id)
                .orElseThrow(() -> new PasteNotFoundException("paste not found"));

        if (record.readCount() >= record.maxReads()) {
            storageService.deleteById(id);
            throw new ReadLimitReachedException("paste has been deleted");
        }

        int nextReadCount = record.readCount() + 1;
        boolean deleteAfterRead = nextReadCount >= record.maxReads();

        PasteRecord updated = new PasteRecord(
                record.id(),
                record.iv(),
                record.ciphertext(),
                record.type(),
                record.language(),
                record.createdAt(),
                record.maxReads(),
                nextReadCount
        );

        if (deleteAfterRead) {
            storageService.deleteById(id);
        } else {
            storageService.save(updated);
        }

        return new PasteReadResult(record, deleteAfterRead);
    }
}
