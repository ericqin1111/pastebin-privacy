package com.example.pastebin.service;

import com.example.pastebin.api.PasteNotFoundException;
import com.example.pastebin.api.ReadLimitReachedException;
import com.example.pastebin.model.PasteRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasteReadServiceTest {

    @Mock
    private PasteStorageService storageService;

    @InjectMocks
    private PasteReadService pasteReadService;

    @Captor
    private ArgumentCaptor<PasteRecord> recordCaptor;

    @Test
    void read_incrementsReadCount_andKeepsRecord() {
        PasteRecord record = new PasteRecord(
                "id",
                "iv",
                "cipher",
                "plain",
                null,
                Instant.now(),
                3,
                1
        );

        when(storageService.findById("id")).thenReturn(Optional.of(record));

        PasteReadResult result = pasteReadService.read("id");

        verify(storageService).save(recordCaptor.capture());
        verify(storageService, never()).deleteById(anyString());

        PasteRecord updated = recordCaptor.getValue();
        assertThat(updated.readCount()).isEqualTo(2);
        assertThat(result.record().id()).isEqualTo("id");
        assertThat(result.deleted()).isFalse();
    }

    @Test
    void read_deletesWhenLimitReached() {
        PasteRecord record = new PasteRecord(
                "id",
                "iv",
                "cipher",
                "plain",
                null,
                Instant.now(),
                2,
                1
        );

        when(storageService.findById("id")).thenReturn(Optional.of(record));

        PasteReadResult result = pasteReadService.read("id");

        verify(storageService).deleteById("id");
        verify(storageService, never()).save(any(PasteRecord.class));
        assertThat(result.deleted()).isTrue();
    }

    @Test
    void read_throwsWhenAlreadyDeleted() {
        PasteRecord record = new PasteRecord(
                "id",
                "iv",
                "cipher",
                "plain",
                null,
                Instant.now(),
                1,
                1
        );

        when(storageService.findById("id")).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> pasteReadService.read("id"))
                .isInstanceOf(ReadLimitReachedException.class);
        verify(storageService).deleteById("id");
    }

    @Test
    void read_throwsWhenMissing() {
        when(storageService.findById("id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pasteReadService.read("id"))
                .isInstanceOf(PasteNotFoundException.class);
    }
}
