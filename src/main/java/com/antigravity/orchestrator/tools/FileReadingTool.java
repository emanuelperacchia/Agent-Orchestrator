package com.antigravity.orchestrator.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Slf4j
public class FileReadingTool {

    @Tool("Reads the content of a file at the specified absolute path. Use this to understand existing code before modifying it.")
    public String readFile(String absolutePath) {
        try {
            Path path = Paths.get(absolutePath);
            if (!Files.exists(path)) {
                return "Error: File does not exist at " + absolutePath;
            }
            log.info("Agent reading file: {}", absolutePath);
            return Files.readString(path);
        } catch (IOException e) {
            log.error("Failed to read file: {}", absolutePath, e);
            return "Error reading file: " + e.getMessage();
        }
    }
}
