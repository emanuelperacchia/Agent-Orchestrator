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
public class FileWritingTool {

    private final WorkspaceValidator workspaceValidator;

    public FileWritingTool(WorkspaceValidator workspaceValidator) {
        this.workspaceValidator = workspaceValidator;
    }

    @Tool("Writes content to a file. Provide a path relative to the workspace or an absolute path within the workspace. Creates parent directories if they don't exist.")
    public String writeToFile(String pathString, String content) {
        try {
            Path path = workspaceValidator.validateAndResolvePath(pathString);
            Files.createDirectories(path.getParent());
            Files.writeString(path, content);
            log.info("Successfully wrote to file: {}", pathString);
            return "File successfully written to " + pathString;
        } catch (IOException | SecurityException e) {
            log.error("Failed to write to file: {}", pathString, e);
            return "Error writing to file: " + e.getMessage();
        }
    }
}
