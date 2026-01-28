package com.example.pastebin.api;

import com.example.pastebin.service.PasteReadResult;
import com.example.pastebin.service.PasteReadService;
import com.example.pastebin.service.PasteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pastes")
public class PasteController {

    private final PasteService pasteService;
    private final PasteReadService pasteReadService;

    public PasteController(PasteService pasteService, PasteReadService pasteReadService) {
        this.pasteService = pasteService;
        this.pasteReadService = pasteReadService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatePasteResponse createPaste(@Valid @RequestBody CreatePasteRequest request) {
        String id = pasteService.createPaste(
                request.getIv(),
                request.getCiphertext(),
                request.getType(),
                request.getLanguage(),
                request.getMaxReads(),
                request.getSizeBytes()
        );
        return new CreatePasteResponse(id);
    }

    @GetMapping("/{id}")
    public GetPasteResponse getPaste(@PathVariable String id) {
        PasteReadResult result = pasteReadService.read(id);
        return GetPasteResponseMapper.from(result.record(), result.readCountAfter());
    }
}
