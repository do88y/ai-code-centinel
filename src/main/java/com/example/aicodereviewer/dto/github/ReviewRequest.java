
package com.example.aicodereviewer.dto.github;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReviewRequest {
    private String event;
    private String body;
    private List<ReviewComment> comments;
}
