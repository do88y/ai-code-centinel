
package com.example.aicodereviewer.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final AnthropicProperties anthropicProperties;
    private final GithubProperties githubProperties;

    @Bean
    public WebClient anthropicWebClient() {
        return WebClient.builder()
            .baseUrl(anthropicProperties.getUrl())
            .defaultHeader("x-api-key", anthropicProperties.getApiKey())
            .defaultHeader("anthropic-version", "2023-06-01")
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    @Bean
    public WebClient githubWebClient() {
        return WebClient.builder()
            .baseUrl(githubProperties.getApiUrl())
            .defaultHeader("Accept", "application/vnd.github.v3+json")
            .build();
    }

    // Note: This is a simplified JWT generation. In a real application, consider caching the JWT.
    private String generateJwt() {
        try {
            String pkcs8Pem = githubProperties.getPrivateKey()
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END RSA PRIVATE KEY-----", "");

            byte[] encoded = Base64.getDecoder().decode(pkcs8Pem);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);

            Algorithm algorithm = Algorithm.RSA256(null, privateKey);

            long nowMillis = System.currentTimeMillis();
            Date now = new Date(nowMillis);
            Date exp = new Date(nowMillis + 10 * 60 * 1000); // 10 minutes expiration

            return JWT.create()
                .withIssuer(githubProperties.getAppId())
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .sign(algorithm);

        } catch (Exception e) {
            throw new RuntimeException("Error generating JWT", e);
        }
    }
}
