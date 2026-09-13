package com.financeai.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class AiChatRequest {
    @NotBlank(message = "Message is required")
    private String message;

    private List<ConversationMessage> conversationHistory;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<ConversationMessage> getConversationHistory() { return conversationHistory; }
    public void setConversationHistory(List<ConversationMessage> conversationHistory) {
        this.conversationHistory = conversationHistory;
    }

    public static class ConversationMessage {
        private String role;
        private String content;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
