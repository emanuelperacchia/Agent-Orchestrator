package com.antigravity.orchestrator.tools;

import com.antigravity.orchestrator.config.SecuritySettings;
import com.antigravity.orchestrator.repository.AuditLogRepository;
import com.antigravity.orchestrator.repository.SecuritySettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SecurityConfigToolTest {

    private SecuritySettings securitySettings;
    private SecuritySettingsRepository settingsRepository;
    private AuditLogRepository auditLogRepository;
    private SecurityConfigTool securityConfigTool;

    @BeforeEach
    void setUp() {
        securitySettings = new SecuritySettings();
        settingsRepository = mock(SecuritySettingsRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);
        
        when(settingsRepository.findById(1L)).thenReturn(Optional.of(securitySettings));
        when(settingsRepository.save(any(SecuritySettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        securityConfigTool = new SecurityConfigTool(settingsRepository, auditLogRepository);
    }

    @Test
    void testInitialSettingsAreFalse() {
        assertThat(securitySettings.isSecurityChecks()).isFalse();
        assertThat(securitySettings.isAuditLogs()).isFalse();
    }

    @Test
    void testUpdateAllSettings() {
        Map<String, Boolean> config = new HashMap<>();
        config.put("securityChecks", true);
        config.put("auditLogs", true);
        config.put("twoFactorAuth", true);
        config.put("secureConnections", true);
        config.put("dataEncryption", true);
        config.put("accessControls", true);
        config.put("userAuthentication", true);
        config.put("backupAndRestore", true);

        String result = securityConfigTool.updateConfig(config);

        assertThat(result).contains("successfully");
        assertThat(securitySettings.isSecurityChecks()).isTrue();
        assertThat(securitySettings.isAuditLogs()).isTrue();
        
        verify(settingsRepository, times(1)).save(securitySettings);
        verify(auditLogRepository, times(1)).save(any());
    }

    @Test
    void testPartialUpdate() {
        Map<String, Boolean> config = new HashMap<>();
        config.put("securityChecks", true);

        securityConfigTool.updateConfig(config);

        assertThat(securitySettings.isSecurityChecks()).isTrue();
        assertThat(securitySettings.isDataEncryption()).isFalse();
    }

    @Test
    void testInvalidKeyDoesNotAffectSettings() {
        Map<String, Boolean> config = new HashMap<>();
        config.put("invalidKey", true);

        securityConfigTool.updateConfig(config);

        assertThat(securitySettings.isSecurityChecks()).isFalse();
    }
}
