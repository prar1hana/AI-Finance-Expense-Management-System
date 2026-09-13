package com.financeai.controller;

import com.financeai.dto.request.AiChatRequest;
import com.financeai.dto.response.AiCategoryResponse;
import com.financeai.dto.response.AiChatResponse;
import com.financeai.entity.User;
import com.financeai.service.AiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/categorize")
    public ResponseEntity<AiCategoryResponse> categorize(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> body) {
        String description = body.get("description");
        if (description == null || description.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(aiService.suggestCategory(description));
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AiChatRequest request) {
        return ResponseEntity.ok(aiService.chat(user.getId(), user.getName(), request));
    }
}
