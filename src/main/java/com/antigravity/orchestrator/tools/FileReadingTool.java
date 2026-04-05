package com.antigravity.orchestrator.tools;

import com.antigravity.orchestrator.security.WorkspaceValidator;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Slf4j
public class FileReadingTool {

    private final WorkspaceValidator workspaceValidator;

    public FileReadingTool(WorkspaceValidator workspaceValidator) {
        this.workspaceValidator = workspaceValidator;
    }

    @Tool("Reads the content of a file within the workspace. Provide relative path or absolute path within the workspace. Use this to understand existing code before modifying it.")
    public String readFile(String pathString) {
        try {
            Path path = workspaceValidator.validateAndResolvePath(pathString);
            if (!Files.exists(path)) {
                return "Error: File does not exist at " + pathString;
            }
            log.info("Agent reading file: {}", pathString);
            return Files.readString(path);
        } catch (IOException | SecurityException e) {
            log.error("Failed to read file: {}", pathString, e);
            return "Error reading file: " + e.getMessage();
        }
    }
}
