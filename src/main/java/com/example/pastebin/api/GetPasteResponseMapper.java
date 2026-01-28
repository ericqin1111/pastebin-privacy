package com.example.pastebin.api;

import com.example.pastebin.model.PasteRecord;

public final class GetPasteResponseMapper {

    private GetPasteResponseMapper() {
    }

    public static GetPasteResponse from(PasteRecord record, int readCountAfter) {
        return new GetPasteResponse(
                record.id(),
                record.iv(),
                record.ciphertext(),
                record.type(),
                record.language(),
                record.maxReads(),
                readCountAfter
        );
    }
}
