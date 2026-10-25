package com.finflow.ticketmcp.tools;

import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;

@Component
public class YouTrackResources {

    @McpResource(
        uri = "finflow://youtrack/workflow",
        name = "YouTrack Workflow Guide",
        description = "Explains the team's issue workflow",
        mimeType = "text/markdown"
    )
    public String getWorkflowGuide() {
        return """
            # YouTrack Workflow

            ## Issue status

            - Open: Work has not started.
            - In Progress: Someone is working on the issue.
            - Resolved: The team considers the issue resolved.
            - Closed: The team has closed the issue.

            Check the actual issue status in YouTrack
            before reporting its current state.
            """;
    }
}
