package com.antigravity.orchestrator.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CommandLineExecutionTool {

    @Tool("Executes a shell command in the specified directory. Use this to compile code, run tests, or install dependencies. Returns the console output.")
    public String executeCommand(String command, String workingDirectory) {
        log.info("Agent executing command: '{}' in '{}'", command, workingDirectory);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("cmd.exe", "/c", command);
            processBuilder.directory(new File(workingDirectory));
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(120, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                return "Error: Command timed out after 120 seconds. Output so far:\n" + output.toString();
            }

            return "Command finished with exit code " + process.exitValue() + ".\nOutput:\n" + output.toString();

        } catch (IOException | InterruptedException e) {
            log.error("Failed to execute command: {}", command, e);
            Thread.currentThread().interrupt();
            return "Error executing command: " + e.getMessage();
        }
    }
}
