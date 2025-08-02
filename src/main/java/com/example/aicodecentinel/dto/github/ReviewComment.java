
package com.example.aicodecentinel.dto.github;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewComment {
    private String path;
    private int position;
    private String body;
}
