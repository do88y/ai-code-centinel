
package com.example.aicodereviewer.dto.github;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewComment {
    private String path;
    private int position;
    private String body;
}
