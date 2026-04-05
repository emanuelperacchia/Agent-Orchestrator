package com.antigravity.orchestrator.core;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AuditLog stores security-sensitive events including configuration changes
 * and tool executions.
 */
@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;

    private String action; // CONFIG_UPDATE, COMMAND_EXECUTION, FILE_ACCESS

    @Column(columnDefinition = "TEXT")
    private String details;

    private String status; // SUCCESS, DENIED, FAILED

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
