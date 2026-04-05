package com.antigravity.orchestrator.tools;

import com.antigravity.orchestrator.security.WorkspaceValidator;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class DirectoryListingTool {

    private final WorkspaceValidator workspaceValidator;

    public DirectoryListingTool(WorkspaceValidator workspaceValidator) {
        this.workspaceValidator = workspaceValidator;
    }

    @Tool("Lists all files and directories inside the specified path within the workspace. Provide a path relative to the workspace. Use this to discover the structure of a project.")
    public String listDirectory(String pathString) {
        try {
            Path dirPath = workspaceValidator.validateAndResolvePath(pathString);
            if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
                return "Error: Directory does not exist or is not a directory: " + pathString;
            }
            log.info("Agent listing directory: {}", pathString);
            try (Stream<Path> stream = Files.list(dirPath)) {
                return stream.map(p -> p.getFileName().toString() + (Files.isDirectory(p) ? "/" : ""))
                             .collect(Collectors.joining("\n"));
            }
        } catch (IOException | SecurityException e) {
            log.error("Failed to list directory: {}", pathString, e);
            return "Error listing directory: " + e.getMessage();
        }
    }
}
