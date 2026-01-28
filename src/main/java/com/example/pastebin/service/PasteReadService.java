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
        AtomicReadResult result = storageService.readAndIncrement(id);
        if (result.status() == ReadStatus.MISSING) {
            throw new PasteNotFoundException("paste not found");
        }
        if (result.status() == ReadStatus.LIMIT_REACHED) {
            throw new ReadLimitReachedException("paste has been deleted");
        }
        return new PasteReadResult(result.record(), result.readCountAfter(), result.deletedAfterRead());
    }
}
