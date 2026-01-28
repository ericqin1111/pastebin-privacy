package com.example.pastebin.service;

import com.example.pastebin.config.PasteProperties;
import com.example.pastebin.model.PasteRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class PasteStorageService {

    private static final String KEY_PREFIX = "paste:";

    private final RedisTemplate<String, PasteRecord> redisTemplate;
    private final PasteProperties properties;

    public PasteStorageService(RedisTemplate<String, PasteRecord> redisTemplate, PasteProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public void save(PasteRecord record) {
        String key = keyFor(record.id());
        Duration ttl = Duration.ofMinutes(properties.getExpireTime());
        redisTemplate.opsForValue().set(key, record, ttl);
    }

    public Optional<PasteRecord> findById(String id) {
        PasteRecord record = redisTemplate.opsForValue().get(keyFor(id));
        return Optional.ofNullable(record);
    }

    public void deleteById(String id) {
        redisTemplate.delete(keyFor(id));
    }

    private String keyFor(String id) {
        return KEY_PREFIX + id;
    }
}
