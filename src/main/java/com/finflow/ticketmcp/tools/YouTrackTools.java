package com.finflow.ticketmcp.tools;

import com.finflow.ticketmcp.youtrack.YouTrackClient;
import com.finflow.ticketmcp.youtrack.YouTrackIssue;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

@Component
public class YouTrackTools {

  private final YouTrackClient youTrackClient;

  @Value("${youtrack.project-id}")
  private String projectId;

  public YouTrackTools(YouTrackClient youTrackClient) {
    this.youTrackClient = youTrackClient;
  }

  @McpTool(description = "Get a page of issues from YouTrack")
  public List<YouTrackIssue> getYouTrackIssues(
      @McpToolParam(
          description = "Maximum number of issues to return (1-100)"
      ) int limit,
      @McpToolParam(
          description = "Number of issues to skip; must be zero or greater"
      ) int offset) {

    if (limit < 1 || limit > 100) {
      throw new IllegalArgumentException(
          "Limit must be between 1 and 100");
    }

    if (offset < 0) {
      throw new IllegalArgumentException(
          "Offset must be zero or greater");
    }

    return youTrackClient.getIssues(limit, offset);
  }

  @McpTool(description = "Get a specific YouTrack issue by its issue ID")
  public YouTrackIssueResult getYouTrackIssue(
      @McpToolParam(
          description = "The readable YouTrack issue ID, for example FIN-123"
      ) String issueId) {

    if (issueId == null || issueId.isBlank()) {
      throw new IllegalArgumentException("Issue ID must not be blank");
    }

    String normalizedId = issueId.trim();

    if (!normalizedId.matches("[A-Z][A-Z0-9]*-\\d+")) {
      throw new IllegalArgumentException(
          "Issue ID must match the format PROJECT-123, e.g. DEMO-123");
    }

    try {
      return youTrackClient.getIssue(normalizedId)
          .map(issue -> new YouTrackIssueResult(
              "FOUND", issue, null))
          .orElseGet(() -> new YouTrackIssueResult(
              "NOT_FOUND", null,
              "No YouTrack issue exists with ID " + normalizedId + "."
          ));

    } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized ex) {
      return new YouTrackIssueResult(
          "AUTHENTICATION_FAILED", null,
          "YouTrack rejected the configured credentials."
      );
    } catch (HttpClientErrorException.Forbidden ex) {
      return new YouTrackIssueResult(
          "PERMISSION_DENIED",
          null,
          "The configured YouTrack credentials do not have permission to access issues."
      );
    } catch (ResourceAccessException ex) {
      return new YouTrackIssueResult(
          "SERVICE_UNAVAILABLE", null,
          "YouTrack could not be reached. Please try again later."
      );
    }
  }

  public record YouTrackIssueResult(
      String status,
      YouTrackIssue issue,
      String message
  ) {

  }

  @McpTool(description = """
      Create a YouTrack issue after the user explicitly approves
      the proposed issue draft. Do not call this tool before
      approval.
      """)
  public YouTrackClient.YouTrackIssueCreated createYouTrackIssue(
      @McpToolParam(description = "Issue title")
      String summary,

      @McpToolParam(description = "Issue description")
      String description) {

    if (summary == null || summary.isBlank()) {
      throw new IllegalArgumentException(
          "Issue summary must not be blank");
    }

    if (description == null || description.isBlank()) {
      throw new IllegalArgumentException(
          "Issue description must not be blank");
    }

    return youTrackClient.createIssue(
        projectId,
        summary.trim(),
        description.trim());
  }

  @McpTool(description = """
      Search YouTrack issues using the native YouTrack search query.
      Returns matching issues with pagination.
      """)
  public List<YouTrackIssue> searchYouTrackIssues(
      @McpToolParam(
          description = "YouTrack search query, for example VPN or GitLab"
      ) String query,

      @McpToolParam(
          description = "Maximum number of issues to return (1-100)"
      ) int limit,

      @McpToolParam(
          description = "Number of issues to skip; must be zero or greater"
      ) int offset) {

    if (query == null || query.isBlank()) {
      throw new IllegalArgumentException(
          "Search query must not be blank");
    }

    if (limit < 1 || limit > 100) {
      throw new IllegalArgumentException(
          "Limit must be between 1 and 100");
    }

    if (offset < 0) {
      throw new IllegalArgumentException(
          "Offset must be zero or greater");
    }

    return youTrackClient.searchIssues(
        query.trim(), limit, offset);
  }
}
