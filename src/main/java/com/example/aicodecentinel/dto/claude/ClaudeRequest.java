
package com.example.aicodecentinel.dto.claude;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ClaudeRequest {
    private String model;
    private List<Message> messages;
    @JsonProperty("max_tokens")
    private int maxTokens;

    @Getter
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}
