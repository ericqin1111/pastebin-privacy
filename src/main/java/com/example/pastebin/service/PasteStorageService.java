package com.example.pastebin.service;

import com.example.pastebin.config.PasteProperties;
import com.example.pastebin.model.PasteRecord;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class PasteStorageService {

    private static final String KEY_PREFIX = "paste:";
    private static final String READ_AND_INCREMENT_LUA = """
            local key = KEYS[1]
            local value = redis.call('GET', key)
            if not value then
                return {'MISSING'}
            end
            local record = cjson.decode(value)
            if record.readCount == nil then
                record.readCount = 0
            end
            if record.maxReads == nil then
                return {'MISSING'}
            end
            if record.readCount >= record.maxReads then
                redis.call('DEL', key)
                return {'LIMIT'}
            end
            local nextRead = record.readCount + 1
            record.readCount = nextRead
            local deleteAfter = nextRead >= record.maxReads
            local encoded = cjson.encode(record)
            if deleteAfter then
                redis.call('DEL', key)
            else
                local ttl = redis.call('PTTL', key)
                if ttl > 0 then
                    redis.call('PSETEX', key, ttl, encoded)
                else
                    redis.call('SET', key, encoded)
                end
            end
            return {'OK', encoded, tostring(nextRead), deleteAfter and '1' or '0'}
            """;

    private final RedisTemplate<String, PasteRecord> redisTemplate;
    private final PasteProperties properties;
    private final StringRedisSerializer stringSerializer = new StringRedisSerializer();

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

    public AtomicReadResult readAndIncrement(String id) {
        String key = keyFor(id);
        List<Object> result = redisTemplate.execute((RedisCallback<List<Object>>) connection -> {
            byte[] keyBytes = stringSerializer.serialize(key);
            byte[] scriptBytes = READ_AND_INCREMENT_LUA.getBytes(StandardCharsets.UTF_8);
            return (List<Object>) connection.eval(scriptBytes, ReturnType.MULTI, 1, keyBytes);
        });

        if (result == null || result.isEmpty()) {
            return new AtomicReadResult(ReadStatus.MISSING, null, 0, false);
        }

        String status = asString(result.get(0));
        if ("MISSING".equals(status)) {
            return new AtomicReadResult(ReadStatus.MISSING, null, 0, false);
        }
        if ("LIMIT".equals(status)) {
            return new AtomicReadResult(ReadStatus.LIMIT_REACHED, null, 0, false);
        }
        if (!"OK".equals(status) || result.size() < 4) {
            return new AtomicReadResult(ReadStatus.MISSING, null, 0, false);
        }

        PasteRecord record = deserializeRecord(result.get(1));
        int readCountAfter = parseInt(result.get(2));
        boolean deletedAfterRead = "1".equals(asString(result.get(3)));
        return new AtomicReadResult(ReadStatus.OK, record, readCountAfter, deletedAfterRead);
    }

    public void deleteById(String id) {
        redisTemplate.delete(keyFor(id));
    }

    private String keyFor(String id) {
        return KEY_PREFIX + id;
    }

    private String asString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof byte[] bytes) {
            return stringSerializer.deserialize(bytes);
        }
        return value.toString();
    }

    private int parseInt(Object value) {
        if (value instanceof Long longValue) {
            return longValue.intValue();
        }
        String text = asString(value);
        if (text == null || text.isBlank()) {
            return 0;
        }
        return Integer.parseInt(text);
    }

    private PasteRecord deserializeRecord(Object value) {
        if (value instanceof byte[] bytes) {
            Object decoded = redisTemplate.getValueSerializer().deserialize(bytes);
            return (PasteRecord) decoded;
        }
        String text = asString(value);
        if (text == null) {
            return null;
        }
        Object decoded = redisTemplate.getValueSerializer().deserialize(text.getBytes(StandardCharsets.UTF_8));
        return (PasteRecord) decoded;
    }
}
