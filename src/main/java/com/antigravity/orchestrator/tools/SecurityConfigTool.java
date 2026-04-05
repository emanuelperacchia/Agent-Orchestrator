package com.antigravity.orchestrator.tools;

import com.antigravity.orchestrator.config.SecuritySettings;
import com.antigravity.orchestrator.core.AuditLog;
import com.antigravity.orchestrator.repository.AuditLogRepository;
import com.antigravity.orchestrator.repository.SecuritySettingsRepository;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Tool for updating the security configuration of the Agent Orchestrator.
 * Now persists settings and logs updates for security auditing.
 */
@Component
@Slf4j
public class SecurityConfigTool {

    private final SecuritySettingsRepository settingsRepository;
    private final AuditLogRepository auditLogRepository;

    public SecurityConfigTool(SecuritySettingsRepository settingsRepository, AuditLogRepository auditLogRepository) {
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

    @Tool("Updates the security configuration of the orchestrator. Provide a map of security flags and their new boolean values. Results are persisted and audited.")
    @Transactional
    public String updateConfig(Map<String, Boolean> config) {
        log.info("Updating security configuration: {}", config);

        SecuritySettings settings = getSettings();

        if (config.containsKey("securityChecks")) settings.setSecurityChecks(config.get("securityChecks"));
        if (config.containsKey("auditLogs")) settings.setAuditLogs(config.get("auditLogs"));
        if (config.containsKey("twoFactorAuth")) settings.setTwoFactorAuth(config.get("twoFactorAuth"));
        if (config.containsKey("secureConnections")) settings.setSecureConnections(config.get("secureConnections"));
        if (config.containsKey("dataEncryption")) settings.setDataEncryption(config.get("dataEncryption"));
        if (config.containsKey("accessControls")) settings.setAccessControls(config.get("accessControls"));
        if (config.containsKey("userAuthentication")) settings.setUserAuthentication(config.get("userAuthentication"));
        if (config.containsKey("backupAndRestore")) settings.setBackupAndRestore(config.get("backupAndRestore"));

        settingsRepository.save(settings);

        // Audit Log entry
        if (settings.isAuditLogs()) {
            auditLogRepository.save(AuditLog.builder()
                    .action("CONFIG_UPDATE")
                    .details("Configuration updated with: " + config.toString())
                    .status("SUCCESS")
                    .build());
        }

        return "Persistent security configuration updated successfully: " + settings.toString();
    }
}
