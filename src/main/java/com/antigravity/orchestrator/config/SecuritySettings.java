package com.antigravity.orchestrator.config;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Persistable SecuritySettings entity for storing the orchestrator configuration.
 */
@Entity
@Table(name = "security_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecuritySettings {

    @Id
    @Builder.Default
    private Long id = 1L; // Static ID for the single row of settings

    @Builder.Default
    private boolean securityChecks = false;
    @Builder.Default
    private boolean auditLogs = false;
    @Builder.Default
    private boolean twoFactorAuth = false;
    @Builder.Default
    private boolean secureConnections = false;
    @Builder.Default
    private boolean dataEncryption = false;
    @Builder.Default
    private boolean accessControls = false;
    @Builder.Default
    private boolean userAuthentication = false;
    @Builder.Default
    private boolean backupAndRestore = false;
}
