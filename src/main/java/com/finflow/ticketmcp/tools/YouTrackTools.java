package com.finflow.ticketmcp.tools;

import com.finflow.ticketmcp.youtrack.YouTrackClient;
import com.finflow.ticketmcp.youtrack.YouTrackClient.YouTrackIssue;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class YouTrackTools {

  private final YouTrackClient youTrackClient;

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
    } catch (org.springframework.web.client.HttpClientErrorException.Forbidden ex) {
      return new YouTrackIssueResult(
          "AUTHENTICATION_FAILED", null,
          "The configured YouTrack credentials do not have permission to access issues."
      );
    }
  }

  public record YouTrackIssueResult(
      String status,
      YouTrackClient.YouTrackIssue issue,
      String message
  ) {

  }
}
