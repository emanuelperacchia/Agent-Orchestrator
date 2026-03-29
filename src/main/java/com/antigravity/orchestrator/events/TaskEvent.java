package com.antigravity.orchestrator.events;

import org.springframework.context.ApplicationEvent;

public class TaskEvent extends ApplicationEvent {
    private final Long taskId;

    public TaskEvent(Object source, Long taskId) {
        super(source);
        this.taskId = taskId;
    }
    
    public Long getTaskId() {
        return taskId;
    }
}
