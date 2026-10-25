package com.finflow.ticketmcp.youtrack;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

@Component
public class YouTrackClient {

    private final RestClient restClient;

    public YouTrackClient(
            @Value("${youtrack.base-url}") String baseUrl,
            @Value("${youtrack.token}") String token) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(
                        "Authorization",
                        "Bearer " + token)
                .defaultHeader(
                        "Accept",
                        "application/json")
                .build();
    }

    public List<YouTrackIssue> getIssues(int limit) {
        return restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/issues")
                .queryParam(
                    "fields",
                    "idReadable,summary,customFields(name,value(name))"
                )
                .queryParam("$top", limit)
                .build())
            .retrieve()
            .body(new org.springframework.core.ParameterizedTypeReference<>() {});
    }


    public YouTrackIssue getIssue(String issueId) {
        return restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/issues/{issueId}")
                .queryParam(
                    "fields",
                    "idReadable,summary,customFields(name,value(name))"
                )
                .build(issueId))
            .retrieve()
            .body(YouTrackIssue.class);
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
