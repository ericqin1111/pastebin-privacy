package com.example.pastebin;

import com.example.pastebin.api.CreatePasteRequest;
import com.example.pastebin.service.AtomicReadResult;
import com.example.pastebin.service.PasteStorageService;
import com.example.pastebin.service.ReadStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PasteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PasteStorageService storageService;

    @Test
    void createPaste_returnsId() throws Exception {
        CreatePasteRequest request = new CreatePasteRequest();
        request.setIv("iv");
        request.setCiphertext("cipher");
        request.setType("plain");
        request.setLanguage(null);
        request.setMaxReads(1);
        request.setSizeBytes(10L);

        doNothing().when(storageService).save(any());

        mockMvc.perform(post("/api/pastes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createPaste_validatesPayload() throws Exception {
        Map<String, Object> request = Map.of(
                "iv", "",
                "ciphertext", "",
                "type", "",
                "maxReads", 0,
                "sizeBytes", 0
        );

        mockMvc.perform(post("/api/pastes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void getPaste_returnsNotFound() throws Exception {
        when(storageService.readAndIncrement("missing"))
                .thenReturn(new AtomicReadResult(
                        ReadStatus.MISSING,
                        null,
                        0,
                        false
                ));

        mockMvc.perform(get("/api/pastes/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
