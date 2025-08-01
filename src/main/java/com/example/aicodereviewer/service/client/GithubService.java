package com.example.aicodereviewer.service.client;

import com.example.aicodereviewer.config.GithubProperties;
import com.example.aicodereviewer.dto.github.CommentRequest;
import com.example.aicodereviewer.dto.github.Content;
import com.example.aicodereviewer.dto.github.ReviewRequest;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubService {

    private final WebClient githubWebClient;
    private final GithubProperties githubProperties;

    public Mono<String> getGuidelineFileContent(String repositoryFullName) {
        return getInstallationAccessToken(repositoryFullName)
            .flatMap(accessToken -> githubWebClient.get()
                .uri("/repos/{owner}/{repo}/contents/CENTINEL.md",
                    repositoryFullName.split("/")[0],
                    repositoryFullName.split("/")[1])
                .header(HttpHeaders.AUTHORIZATION, "token " + accessToken)
                .retrieve()
                .bodyToMono(Content.class)
                .map(Content::getDecodedContent)
                .onErrorResume(WebClientResponseException.class,
                    ex -> ex.getRawStatusCode() == 404 ? Mono.empty() : Mono.error(ex))
            ).defaultIfEmpty("");
    }

    public Mono<String> getPullRequestDiff(String diffUrl) {
        return githubWebClient.get()
            .uri(diffUrl)
            .header(HttpHeaders.ACCEPT, "application/vnd.github.v3.diff")
            .retrieve()
            .bodyToMono(String.class);
    }

    public Mono<Void> postComment(String repositoryFullName, int pullRequestNumber, String comment) {
        return getInstallationAccessToken(repositoryFullName)
            .flatMap(accessToken -> {
                CommentRequest commentRequest = CommentRequest.builder()
                    .body(comment)
                    .build();
                return githubWebClient.post()
                    .uri("/repos/{owner}/{repo}/issues/{issue_number}/comments",
                        repositoryFullName.split("/")[0],
                        repositoryFullName.split("/")[1],
                        pullRequestNumber)
                    .header(HttpHeaders.AUTHORIZATION, "token " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(commentRequest)
                    .retrieve()
                    .bodyToMono(Void.class);
            });
    }

    public Mono<Void> submitPullRequestReview(String repositoryFullName, int pullRequestNumber, ReviewRequest reviewRequest) {
        return getInstallationAccessToken(repositoryFullName)
            .flatMap(accessToken -> {
                return githubWebClient.post()
                    .uri("/repos/{owner}/{repo}/pulls/{pull_number}/reviews",
                        repositoryFullName.split("/")[0],
                        repositoryFullName.split("/")[1],
                        pullRequestNumber)
                    .header(HttpHeaders.AUTHORIZATION, "token " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(reviewRequest)
                    .retrieve()
                    .bodyToMono(Void.class);
            });
    }

    private Mono<String> getInstallationAccessToken(String repositoryFullName) {
        return githubWebClient.get()
            .uri("/app/installations")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + generateJwt())
            .retrieve()
            .bodyToFlux(Map.class) // List of installations
            .filter(installation -> repositoryFullName.equals(installation.get("repository_selection").equals("all") ? repositoryFullName : ((Map)installation.get("target_repository")).get("full_name")))
            .next() // Get the first matching installation
            .map(installation -> (String) ((Map)installation.get("access_tokens_url")).get("url"))
            .flatMap(accessTokenUrl -> githubWebClient.post()
                .uri(accessTokenUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + generateJwt())
                .retrieve()
                .bodyToMono(Map.class))
            .map(response -> (String) response.get("token"));
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