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
public class FileWritingTool {

    @Tool("Writes content to a file at the specified absolute path. Creates parent directories if they don't exist.")
    public String writeToFile(String absolutePath, String content) {
        try {
            Path path = Paths.get(absolutePath);
            Files.createDirectories(path.getParent());
            Files.writeString(path, content);
            log.info("Successfully wrote to file: {}", absolutePath);
            return "File successfully written to " + absolutePath;
        } catch (IOException e) {
            log.error("Failed to write to file: {}", absolutePath, e);
            return "Error writing to file: " + e.getMessage();
        }
    }
}
