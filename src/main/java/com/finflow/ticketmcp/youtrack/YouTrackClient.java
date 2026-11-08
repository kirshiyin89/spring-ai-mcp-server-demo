package com.finflow.ticketmcp.youtrack;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

@Component
public class YouTrackClient {

    private final RestClient restClient;

    public YouTrackClient(
        @Value("${youtrack.base-url}") String baseUrl,
        @Value("${youtrack.token}") String token) {

        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

        JdkClientHttpRequestFactory requestFactory =
            new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(requestFactory)
            .defaultHeader("Authorization", "Bearer " + token)
            .defaultHeader("Accept", "application/json")
            .build();
    }


    public List<YouTrackIssue> getIssues(int limit, int offset) {
        return restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/issues")
                .queryParam(
                    "fields",
                    "idReadable,summary,customFields(name,value(name))"
                )
                .queryParam("$top", limit)
                .queryParam("$skip", offset)
                .build())
            .retrieve()
            .body(new org.springframework.core.ParameterizedTypeReference<>() {});
    }


    public Optional<YouTrackIssue> getIssue(String issueId) {
        try {
            YouTrackIssue issue = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/api/issues/{issueId}")
                    .queryParam(
                        "fields",
                        "idReadable,summary,customFields(name,value(name))"
                    )
                    .build(issueId))
                .retrieve()
                .body(YouTrackIssue.class);

            return Optional.ofNullable(issue);
        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();
        }
    }

    public YouTrackIssueCreated createIssue(
        String projectId,
        String summary,
        String description) {

        var request = new CreateIssueRequest(
            new ProjectReference(projectId),
            summary,
            description
        );

        return restClient.post()
            .uri(uriBuilder -> uriBuilder
                .path("/api/issues")
                .queryParam(
                    "fields",
                    "id,idReadable,summary"
                )
                .build())
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(YouTrackIssueCreated.class);
    }


    public record CreateIssueRequest(
        ProjectReference project,
        String summary,
        String description) {
    }

    public record ProjectReference(String id) {
    }

    public record YouTrackIssueCreated(
        String id,
        String idReadable,
        String summary) {
    }

    public record YouTrackIssue(
        String idReadable,
        String summary,
        List<YouTrackCustomField> customFields) {
    }

    public record YouTrackCustomField(
        String name,
        JsonNode value) {
    }

}
