package com.example.pastebin.service;

import com.example.pastebin.model.PasteRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "paste.expire-time=1"
})
class PasteTtlIntegrationTest {

    @Autowired
    private PasteStorageService storageService;

    @Autowired
    private RedisTemplate<String, PasteRecord> redisTemplate;

    @Test
    void pasteExpiresAfterTtl() {
        PasteRecord record = new PasteRecord(
                "ttl-id",
                "iv",
                "cipher",
                "plain",
                null,
                Instant.now(),
                1,
                0
        );

        storageService.save(record);

        redisTemplate.expire("paste:" + record.id(), Duration.ofSeconds(1));

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(storageService.findById(record.id())).isEmpty());
    }
}
