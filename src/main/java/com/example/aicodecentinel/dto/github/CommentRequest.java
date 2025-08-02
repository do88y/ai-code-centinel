
package com.example.aicodecentinel.dto.github;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentRequest {
    private String body;
}
