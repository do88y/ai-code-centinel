
package com.example.aicodereviewer.dto.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Base64;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Content {
    private String content;
    private String encoding;

    public String getDecodedContent() {
        if ("base64".equalsIgnoreCase(encoding) && content != null) {
            byte[] decodedBytes = Base64.getDecoder().decode(content);
            return new String(decodedBytes);
        }
        return content;
    }
}
