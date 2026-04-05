package com.antigravity.orchestrator.tools;

import com.antigravity.orchestrator.config.SecuritySettings;
import com.antigravity.orchestrator.repository.AuditLogRepository;
import com.antigravity.orchestrator.repository.SecuritySettingsRepository;
import com.antigravity.orchestrator.security.WorkspaceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CommandLineExecutionSecurityTest {

    private WorkspaceValidator workspaceValidator;
    private SecuritySettingsRepository settingsRepository;
    private AuditLogRepository auditLogRepository;
    private CommandLineExecutionTool commandTool;
    private SecuritySettings settings;

    @BeforeEach
    void setUp() {
        workspaceValidator = mock(WorkspaceValidator.class);
        settingsRepository = mock(SecuritySettingsRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);
        
        settings = new SecuritySettings();
        when(settingsRepository.findById(1L)).thenReturn(Optional.of(settings));
        
        commandTool = new CommandLineExecutionTool(workspaceValidator, settingsRepository, auditLogRepository);
        
        // Mock workspace behavior
        when(workspaceValidator.getBaseWorkspacePath()).thenReturn(Paths.get("/tmp/workspace"));
        when(workspaceValidator.validateAndResolvePath(any())).thenReturn(Paths.get("/tmp/workspace"));
    }

    @Test
    void testDeniedDangerousCommandWhenSecurityEnabled() {
        settings.setSecurityChecks(true);
        settings.setAuditLogs(true);
        
        String result = commandTool.executeCommand("rm -rf /", ".");
        
        assertThat(result).contains("Security Denied");
        verify(auditLogRepository, times(1)).save(any());
    }

    @Test
    void testAllowedDangerousCommandWhenSecurityDisabled() {
        settings.setSecurityChecks(false);
        // Note: This will attempt to run docker, so we expect an error from the process builder in this environment, 
        // but it should NOT be caught by our security blacklist.
        
        String result = commandTool.executeCommand("rm -rf /", ".");
        
        // It shouldn't contain "Security Denied"
        assertThat(result).doesNotContain("Security Denied");
    }

    @Test
    void testAuditLoggingWhenEnabled() {
        settings.setAuditLogs(true);
        settings.setSecurityChecks(false);
        
        commandTool.executeCommand("ls", ".");
        
        verify(auditLogRepository, atLeastOnce()).save(any());
    }
}
