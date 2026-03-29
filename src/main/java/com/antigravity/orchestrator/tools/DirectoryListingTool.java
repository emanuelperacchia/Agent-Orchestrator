package com.antigravity.orchestrator.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class DirectoryListingTool {

    @Tool("Lists all files and directories inside the specified path. Use this to discover the structure of a project.")
    public String listDirectory(String absolutePath) {
        try {
            Path dirPath = Paths.get(absolutePath);
            if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
                return "Error: Directory does not exist or is not a directory: " + absolutePath;
            }
            log.info("Agent listing directory: {}", absolutePath);
            try (Stream<Path> stream = Files.list(dirPath)) {
                return stream.map(p -> p.getFileName().toString() + (Files.isDirectory(p) ? "/" : ""))
                             .collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            log.error("Failed to list directory: {}", absolutePath, e);
            return "Error listing directory: " + e.getMessage();
        }
    }
}
