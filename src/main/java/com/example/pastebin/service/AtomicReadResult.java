package com.example.pastebin.service;

import com.example.pastebin.model.PasteRecord;

public record AtomicReadResult(ReadStatus status,
                               PasteRecord record,
                               int readCountAfter,
                               boolean deletedAfterRead) {
}
