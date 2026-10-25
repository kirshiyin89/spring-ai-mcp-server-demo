package com.finflow.ticketmcp.tools;

import java.util.List;

import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Component;

import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

@Component
public class YouTrackPrompts {

    @McpPrompt(
        name = "review-youtrack-issue",
        description = "Prepare a structured issue review"
    )
    public GetPromptResult reviewIssue(
        @McpArg(
            name = "issueId",
            description = "The YouTrack issue ID, e.g. FIN-123",
            required = true
        ) String issueId
    ) {
        String message = """
            Review YouTrack issue %s.

            Identify:
            1. The issue summary and current status.
            2. Any missing information.
            3. Suggested next steps.

            Do not invent issue details.
            Retrieve the actual issue details before
            making claims about its status or content.
            """.formatted(issueId);

        return GetPromptResult.builder(List.of(
                new PromptMessage(
                    Role.USER,
                    new TextContent(message)
                )
            ))
            .build();
    }
}
