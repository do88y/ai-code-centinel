
package com.example.aicodereviewer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GitHubPullRequestEvent {

    private String action;
    private Integer number;
    @JsonProperty("pull_request")
    private PullRequest pullRequest;
    private Repository repository;

    @Data
    public static class PullRequest {
        private String url;
        @JsonProperty("diff_url")
        private String diffUrl;
        private String title;
        private String body;
        private String state;
    }

    @Data
    public static class Repository {
        private Long id;
        @JsonProperty("full_name")
        private String fullName;
    }
}
