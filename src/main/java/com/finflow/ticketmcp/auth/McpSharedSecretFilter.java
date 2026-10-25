package com.finflow.ticketmcp.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class McpSharedSecretFilter extends OncePerRequestFilter {

    private static final String MCP_PATH = "/mcp";
    private static final String SECRET_HEADER = "X-MCP-Secret";

    private final String expectedSecret;

    public McpSharedSecretFilter(
            @Value("${app.security.mcp.shared-secret}")
            String expectedSecret) {

        if (expectedSecret == null || expectedSecret.isBlank()) {
            throw new IllegalStateException(
                "MCP shared secret must be configured");
        }

        this.expectedSecret = expectedSecret;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !MCP_PATH.equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String suppliedSecret = request.getHeader(SECRET_HEADER);

        boolean valid = suppliedSecret != null
                && MessageDigest.isEqual(
                    expectedSecret.getBytes(StandardCharsets.UTF_8),
                    suppliedSecret.getBytes(StandardCharsets.UTF_8));

        if (!valid) {
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized MCP client");
            return;
        }

        filterChain.doFilter(request, response);
    }
}