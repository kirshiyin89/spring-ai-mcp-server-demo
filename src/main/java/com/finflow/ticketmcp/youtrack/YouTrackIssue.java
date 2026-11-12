package com.finflow.ticketmcp.youtrack;

import com.finflow.ticketmcp.youtrack.YouTrackClient.YouTrackCustomField;
import java.util.List;

public record YouTrackIssue(
    String idReadable,
    String summary,
    String description,
    List<YouTrackCustomField> customFields
) {

}