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

  @McpTool(description = "Get a list of issues from YouTrack")
  public List<YouTrackIssue> getYouTrackIssues(
      @McpToolParam(
          description = "Maximum number of issues to return"
      ) int limit) {
    if (limit < 1 || limit > 100) {
      throw new IllegalArgumentException(
          "Limit must be between 1 and 100");
    }
    return youTrackClient.getIssues(limit);
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

    return youTrackClient.getIssue(normalizedId)
        .map(issue -> new YouTrackIssueResult(true, issue, null))
        .orElseGet(() -> new YouTrackIssueResult(
            false,
            null,
            "No YouTrack issue exists with ID " + normalizedId + "."
        ));
  }

  public record YouTrackIssueResult(
      boolean found,
      YouTrackClient.YouTrackIssue issue,
      String message
  ) {

  }
}
