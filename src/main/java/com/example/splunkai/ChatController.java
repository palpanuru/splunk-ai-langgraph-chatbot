package com.example.splunkai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService service;

    public ChatController(ChatService service) {
        this.service = service;
    }

    public record ChatRequest(
        @NotBlank @Size(max = 1000) String message,
        @Size(max = 120) String searchText,
        @Min(1) @Max(60) Integer minutes
    ) {}

    public record ChatResponse(String answer, int eventCount) {}

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return service.answer(request.message(),
            request.searchText() == null ? "" : request.searchText(),
            request.minutes() == null ? 30 : request.minutes());
    }
}
