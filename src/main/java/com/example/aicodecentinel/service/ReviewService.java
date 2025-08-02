

package com.example.aicodecentinel.service;

import com.example.aicodecentinel.dto.claude.ClaudeResponse;
import com.example.aicodecentinel.dto.github.ReviewComment;
import com.example.aicodecentinel.dto.github.ReviewRequest;
import com.example.aicodecentinel.service.client.ClaudeService;
import com.example.aicodecentinel.service.client.GithubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final GithubService githubService;
    private final ClaudeService claudeService;

    public Mono<Void> conductCodeReview(Long repositoryId, String repositoryFullName, int pullRequestNumber, String diffUrl, String prTitle, String prBody) {
        Mono<String> diffMono = githubService.getPullRequestDiff(diffUrl);
        Mono<String> guidelineMono = githubService.getGuidelineFileContent(repositoryFullName);

        return Mono.zip(diffMono, guidelineMono)
            .flatMap(tuple -> {
                String codeDiff = tuple.getT1();
                String guideline = tuple.getT2();
                return claudeService.getCodeReview(codeDiff, guideline, prTitle, prBody);
            })
            .flatMap(claudeResponse -> {
                // 1. Post PR Summary and Sequence Diagram
                Mono<Void> summaryAndDiagramMono = Mono.empty();
                if (claudeResponse.getPrSummary() != null && !claudeResponse.getPrSummary().isBlank()) {
                    summaryAndDiagramMono = summaryAndDiagramMono.then(githubService.postComment(repositoryFullName, pullRequestNumber, "## PR Summary\n" + claudeResponse.getPrSummary()));
                }
                if (claudeResponse.getSequenceDiagramMermaid() != null && !claudeResponse.getSequenceDiagramMermaid().isBlank()) {
                    summaryAndDiagramMono = summaryAndDiagramMono.then(githubService.postComment(repositoryFullName, pullRequestNumber, "## Sequence Diagram\n```mermaid\n" + claudeResponse.getSequenceDiagramMermaid() + "\n```"));
                }

                // 2. Submit Review (APPROVE or CHANGES_REQUESTED)
                String reviewEvent = "";
                String reviewBody = claudeResponse.getReviewComments();
                List<ReviewComment> lineComments = null;

                if ("CHANGES_REQUESTED".equals(claudeResponse.getReviewStatus())) {
                    reviewEvent = "REQUEST_CHANGES";
                    if (claudeResponse.getLineComments() != null) {
                        lineComments = claudeResponse.getLineComments().stream()
                            .map(lc -> ReviewComment.builder()
                                .path(lc.getPath())
                                .position(lc.getLine() != null ? lc.getLine() : 1) // IMPORTANT: Claude's 'line' is absolute, GitHub's 'position' is diff hunk relative. This needs proper diff parsing for accuracy.
                                .body(lc.getComment())
                                .build())
                            .collect(Collectors.toList());
                    }
                } else if ("APPROVED".equals(claudeResponse.getReviewStatus())) {
                    reviewEvent = "APPROVE";
                    reviewBody = ""; // No general comments for approval
                    lineComments = List.of(); // No line comments for approval
                } else {
                    log.warn("Unknown review status from Claude: {}", claudeResponse.getReviewStatus());
                    reviewEvent = "COMMENT"; // Fallback to a general comment if status is unknown
                }

                ReviewRequest reviewRequest = ReviewRequest.builder()
                    .event(reviewEvent)
                    .body(reviewBody)
                    .comments(lineComments)
                    .build();

                Mono<Void> submitReviewMono = githubService.submitPullRequestReview(repositoryFullName, pullRequestNumber, reviewRequest);

                return summaryAndDiagramMono.then(submitReviewMono);
            })
            .onErrorResume(e -> {
                log.error("Error during code review process: {}", e.getMessage(), e);
                // Optionally, post a comment about the error to the PR
                return githubService.postComment(repositoryFullName, pullRequestNumber, "AI Code Review failed due to an internal error: " + e.getMessage());
            });
    }
}
