package com.example.pastebin.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "paste")
public class PasteProperties {

    @Min(1)
    private int expireTime;

    @Min(1)
    @Max(1000)
    private int maxReadsLimit;

    @Min(1)
    @Max(100)
    private int maxSize;

    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }

    public int getMaxReadsLimit() {
        return maxReadsLimit;
    }

    public void setMaxReadsLimit(int maxReadsLimit) {
        this.maxReadsLimit = maxReadsLimit;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}
