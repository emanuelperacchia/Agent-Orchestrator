package com.antigravity.orchestrator.security;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Slf4j
public class WorkspaceValidator {

    @Value("${agent.workspace.path:./workspace}")
    private String workspacePathConfig;

    private Path baseWorkspacePath;

    @PostConstruct
    public void init() throws IOException {
        Path path = Paths.get(workspacePathConfig).toAbsolutePath().normalize();
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        // Using toRealPath() ensures we resolve any symlinks to get the true base path
        this.baseWorkspacePath = path.toRealPath();
        log.info("Agent Workspace securely initialized at: {}", this.baseWorkspacePath);
    }

    /**
     * Validates that the requested path does not escape the base workspace.
     * Returns the resolved and safe Path.
     */
    public Path validateAndResolvePath(String requestedPath) {
        Path requested = Paths.get(requestedPath);
        Path targetPath;

        if (requested.isAbsolute()) {
             targetPath = requested.normalize();
        } else {
             targetPath = baseWorkspacePath.resolve(requested).normalize();
        }

        // Prevent Path Traversal and ensure the logic doesn't escape the workspace
        if (!targetPath.startsWith(baseWorkspacePath)) {
            log.warn("Security Alert: Agent attempted Path Traversal or escaped workspace. Requested: {}", requestedPath);
            throw new SecurityException("Access Denied: Path is outside the allowed workspace.");
        }
        
        return targetPath;
    }

    public Path getBaseWorkspacePath() {
        return baseWorkspacePath;
    }
}
