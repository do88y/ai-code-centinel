
package com.example.aicodereviewer.dto.github;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentRequest {
    private String body;
}
