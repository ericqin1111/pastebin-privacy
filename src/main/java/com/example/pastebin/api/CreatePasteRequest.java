package com.example.pastebin.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreatePasteRequest {

    @NotBlank
    private String iv;

    @NotBlank
    private String ciphertext;

    @NotBlank
    private String type;

    private String language;

    @NotNull
    @Min(1)
    @Max(1000)
    private Integer maxReads;

    @NotNull
    @Min(1)
    private Long sizeBytes;

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getCiphertext() {
        return ciphertext;
    }

    public void setCiphertext(String ciphertext) {
        this.ciphertext = ciphertext;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getMaxReads() {
        return maxReads;
    }

    public void setMaxReads(Integer maxReads) {
        this.maxReads = maxReads;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }
}
