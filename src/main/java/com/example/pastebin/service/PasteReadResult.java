package com.example.pastebin.service;

import com.example.pastebin.model.PasteRecord;

public record PasteReadResult(PasteRecord record, boolean deleted) {
}
