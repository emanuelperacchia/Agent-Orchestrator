package com.antigravity.orchestrator.core;

public interface Agent {
    Role getRole();
    String executeTask(Task task, String context);
}
