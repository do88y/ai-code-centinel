
package com.example.aicodecentinel.controller;

import com.example.aicodecentinel.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final ReviewService reviewService;

    public WebhookController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/github")
    public Mono<ResponseEntity<Void>> handleGitHubWebhook(@RequestHeader("X-GitHub-Event") String githubEvent,
                                                  @RequestBody GitHubPullRequestEvent pullRequestEvent) {
        if ("pull_request".equals(githubEvent)) {
            log.info("Received pull request event for repository: {}", pullRequestEvent.getRepository().getFullName());
            return reviewService.conductCodeReview(
                pullRequestEvent.getRepository().getId(),
                pullRequestEvent.getRepository().getFullName(),
                pullRequestEvent.getNumber(),
                pullRequestEvent.getPullRequest().getDiffUrl(),
                pullRequestEvent.getPullRequest().getTitle(),
                pullRequestEvent.getPullRequest().getBody()
            ).thenReturn(ResponseEntity.ok().build());
        }

        return Mono.just(ResponseEntity.ok().build());
    }
}
