package com.example.aicodereviewer.service.client;

import com.example.aicodereviewer.config.AnthropicProperties;
import com.example.aicodereviewer.dto.claude.ClaudeRequest;
import com.example.aicodereviewer.dto.claude.ClaudeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeService {

    private final WebClient anthropicWebClient;
    private final AnthropicProperties anthropicProperties;
    private final ObjectMapper objectMapper;

    public Mono<ClaudeResponse> getCodeReview(String codeDiff, String guideline, String prTitle, String prBody) {
        String prompt = createPrompt(codeDiff, guideline, prTitle, prBody);

        ClaudeRequest.Message userMessage = ClaudeRequest.Message.builder()
            .role("user")
            .content(prompt)
            .build();

        ClaudeRequest request = ClaudeRequest.builder()
            .model(anthropicProperties.getModel())
            .messages(List.of(userMessage))
            .maxTokens(4096)
            .build();

        return anthropicWebClient.post()
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ClaudeResponse.class)
            .doOnNext(response -> {
                if (response.getContent() != null && !response.getContent().isEmpty()) {
                    String jsonString = response.getFirstContentText();
                    try {
                        ClaudeResponse parsedResponse = objectMapper.readValue(jsonString, ClaudeResponse.class);
                        response.setReviewStatus(parsedResponse.getReviewStatus());
                        response.setReviewComments(parsedResponse.getReviewComments());
                        response.setPrSummary(parsedResponse.getPrSummary());
                        response.setSequenceDiagramMermaid(parsedResponse.getSequenceDiagramMermaid());
                        response.setLineComments(parsedResponse.getLineComments());
                    } catch (Exception e) {
                        log.error("Failed to parse Claude response JSON: {}", jsonString, e);
                        // Fallback to raw text if JSON parsing fails
                        response.setReviewStatus("CHANGES_REQUESTED");
                        response.setReviewComments("Failed to parse AI review. Raw response: " + jsonString);
                    }
                }
            });
    }

    private String createPrompt(String codeDiff, String guideline, String prTitle, String prBody) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("You are an AI assistant for code review. Analyze the following Pull Request and provide a structured review in JSON format.\n\n");
        promptBuilder.append("Your response MUST be a valid JSON object with the following keys:\n");
        promptBuilder.append("- `review_status`: 'APPROVED' if no significant issues, 'CHANGES_REQUESTED' if issues are found.\n");
        promptBuilder.append("- `review_comments`: A general comment about the PR. This should be empty if `review_status` is 'APPROVED'.\n");
        promptBuilder.append("- `pr_summary`: A concise summary of the Pull Request.\n");
        promptBuilder.append("- `sequence_diagram_mermaid`: A Mermaid sequence diagram representing the main flow of the changes. If not applicable, provide an empty string.\n");
        promptBuilder.append("- `line_comments`: An array of objects for line-specific comments. Each object must have `path` (file path), `line` (line number in the new file), and `comment` (the review comment). Only include if `review_status` is 'CHANGES_REQUESTED'.\n\n");

        if (guideline != null && !guideline.isBlank()) {
            promptBuilder.append(String.format("Our team's coding guideline is:\n%s\n\n", guideline));
        }

        promptBuilder.append(String.format("Pull Request Title: %s\n", prTitle));
        promptBuilder.append(String.format("Pull Request Body: %s\n\n", prBody));
        promptBuilder.append(String.format("Code Diff:\n%s\n", codeDiff));

        promptBuilder.append("\nProvide your response in JSON format only. Do not include any other text outside the JSON.");

        return promptBuilder.toString();
    }
}