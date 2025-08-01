
package com.example.aicodereviewer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "anthropic")
public class AnthropicProperties {
    private String apiKey;
    private String url;
    private String model;
}
