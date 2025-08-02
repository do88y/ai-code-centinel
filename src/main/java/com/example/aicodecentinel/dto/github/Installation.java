
package com.example.aicodecentinel.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Installation {
    @JsonProperty("access_tokens_url")
    private String accessTokensUrl;
}
