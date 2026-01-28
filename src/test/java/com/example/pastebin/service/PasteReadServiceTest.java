package com.example.pastebin.service;

import com.example.pastebin.api.PasteNotFoundException;
import com.example.pastebin.api.ReadLimitReachedException;
import com.example.pastebin.model.PasteRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasteReadServiceTest {

    @Mock
    private PasteStorageService storageService;

    @InjectMocks
    private PasteReadService pasteReadService;

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

        AtomicReadResult atomicResult = new AtomicReadResult(ReadStatus.OK, record, 2, false);
        when(storageService.readAndIncrement("id")).thenReturn(atomicResult);

        PasteReadResult result = pasteReadService.read("id");

        assertThat(result.record().id()).isEqualTo("id");
        assertThat(result.readCountAfter()).isEqualTo(2);
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

        AtomicReadResult atomicResult = new AtomicReadResult(ReadStatus.OK, record, 2, true);
        when(storageService.readAndIncrement("id")).thenReturn(atomicResult);

        PasteReadResult result = pasteReadService.read("id");

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

        when(storageService.readAndIncrement("id"))
                .thenReturn(new AtomicReadResult(ReadStatus.LIMIT_REACHED, null, 0, false));

        assertThatThrownBy(() -> pasteReadService.read("id"))
                .isInstanceOf(ReadLimitReachedException.class);
    }

    @Test
    void read_throwsWhenMissing() {
        when(storageService.readAndIncrement("id"))
                .thenReturn(new AtomicReadResult(ReadStatus.MISSING, null, 0, false));

        assertThatThrownBy(() -> pasteReadService.read("id"))
                .isInstanceOf(PasteNotFoundException.class);
    }
}
