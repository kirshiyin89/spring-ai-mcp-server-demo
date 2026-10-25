package com.finflow.ticketmcp.controller;

import java.util.List;

import com.finflow.ticketmcp.youtrack.YouTrackClient;
import com.finflow.ticketmcp.youtrack.YouTrackClient.YouTrackIssue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class YouTrackTestController {

    private final YouTrackClient youTrackClient;

    public YouTrackTestController(YouTrackClient youTrackClient) {
        this.youTrackClient = youTrackClient;
    }

    @GetMapping("/api/youtrack/issues")
    public List<YouTrackIssue> getIssues() {
        return youTrackClient.getIssues(5);
    }

}
