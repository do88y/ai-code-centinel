
package com.example.aicodereviewer.dto.claude;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaudeResponse {
    private List<Content> content;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        private String text;
    }

    // New fields for structured response
    @JsonProperty("review_status")
    private String reviewStatus;

    @JsonProperty("review_comments")
    private String reviewComments;

    @JsonProperty("pr_summary")
    private String prSummary;

    @JsonProperty("sequence_diagram_mermaid")
    private String sequenceDiagramMermaid;

    @JsonProperty("line_comments")
    private List<LineComment> lineComments;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LineComment {
        private String path;
        private Integer line;
        private String comment;
    }

    public String getFirstContentText() {
        if (content != null && !content.isEmpty()) {
            return content.get(0).getText();
        }
        return null;
    }
}
