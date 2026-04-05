package com.antigravity.orchestrator.tools;

import com.antigravity.orchestrator.security.WorkspaceValidator;
import com.antigravity.orchestrator.config.SecuritySettings;
import com.antigravity.orchestrator.core.AuditLog;
import com.antigravity.orchestrator.repository.AuditLogRepository;
import com.antigravity.orchestrator.repository.SecuritySettingsRepository;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CommandLineExecutionTool {

    private final WorkspaceValidator workspaceValidator;
    private final SecuritySettingsRepository settingsRepository;
    private final AuditLogRepository auditLogRepository;

    private static final List<String> BLACKLISTED_COMMANDS = Arrays.asList(
            "rm -rf", "chmod", "chown", "mkfs", "dd", "shutdown", "reboot", ":(){ :|:& };:"
    );

    public CommandLineExecutionTool(WorkspaceValidator workspaceValidator, 
                                    SecuritySettingsRepository settingsRepository, 
                                    AuditLogRepository auditLogRepository) {
        this.workspaceValidator = workspaceValidator;
        this.settingsRepository = settingsRepository;
        this.auditLogRepository = auditLogRepository;
    }

    private SecuritySettings getSettings() {
        return settingsRepository.findById(1L).orElseGet(() -> {
            SecuritySettings defaultSettings = new SecuritySettings();
            defaultSettings.setId(1L);
            return settingsRepository.save(defaultSettings);
        });
    }

    @Tool("Executes a shell command in a secure ephemeral Linux (Ubuntu 22.04) Docker container within the workspace. Provide Linux shell commands. Useful for compiling code, running scripts, and managing files inside /workspace. You can use 'apt-get update && apt-get install' to install necessities. Returns the console output.")
    public String executeCommand(String command, String workingDirectory) {
        SecuritySettings settings = getSettings();

        // 1. Security Checks (Blacklist)
        if (settings.isSecurityChecks()) {
            for (String blacklisted : BLACKLISTED_COMMANDS) {
                if (command.contains(blacklisted)) {
                    String errorMsg = "Security Denied: Command contains blacklisted pattern: " + blacklisted;
                    log.warn(errorMsg);
                    
                    if (settings.isAuditLogs()) {
                        auditLogRepository.save(AuditLog.builder()
                                .action("COMMAND_EXECUTION")
                                .details("DENIED: " + command)
                                .status("DENIED")
                                .build());
                    }
                    return "Error: " + errorMsg;
                }
            }
        }

        try {
            Path baseDir = workspaceValidator.getBaseWorkspacePath();
            Path safeDir = workspaceValidator.validateAndResolvePath(workingDirectory);
            Path relativePath = baseDir.relativize(safeDir);
            
            // Container working directory is /workspace + relative path
            String containerWorkingDir = "/workspace";
            if (!relativePath.toString().isEmpty()) {
                containerWorkingDir += "/" + relativePath.toString().replace("\\", "/");
            }
            
            String hostWorkspacePath = baseDir.toAbsolutePath().toString();

            log.info("Agent executing Docker command: '{}' mapped to '{}'", command, containerWorkingDir);

            if (settings.isAuditLogs()) {
                auditLogRepository.save(AuditLog.builder()
                        .action("COMMAND_EXECUTION")
                        .details("Executing: " + command)
                        .status("IN_PROGRESS")
                        .build());
            }

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(
                "docker", "run", "--rm", 
                "--memory", "512m", 
                "-v", hostWorkspacePath + ":/workspace", 
                "-w", containerWorkingDir, 
                "ubuntu:22.04", 
                "bash", "-c", command
            );
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // Aumentar el timeout a 300s para permitir descargas (apt-get install / docker pull ubuntu)
            boolean finished = process.waitFor(300, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                return "Error: Command timed out after 300 seconds. Output so far:\n" + output.toString();
            }

            return "Command finished with exit code " + process.exitValue() + ".\nOutput:\n" + output.toString();

        } catch (InterruptedException e) {
            log.error("Command execution interrupted: {}", command, e);
            Thread.currentThread().interrupt();
            return "Error: Command interrupted.";
        } catch (IOException | SecurityException e) {
            log.error("Failed to execute docker command: {}", command, e);
            return "Error executing command: " + e.getMessage();
        }
    }
}
