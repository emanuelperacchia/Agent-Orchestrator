package com.antigravity.orchestrator.service;

import com.antigravity.orchestrator.core.Task;
import com.antigravity.orchestrator.core.Role;
import com.antigravity.orchestrator.events.TaskEvent;
import com.antigravity.orchestrator.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrchestratorService {

    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void runProjectGeneration(String userPrompt) {
        log.info("Starting project generation. Placing PM task in backlog.");

        Task pmTask = Task.builder()
                .description(userPrompt)
                .assignedTo(Role.PRODUCT_MANAGER)
                .status("PENDING")
                .build();
                
        pmTask = taskRepository.save(pmTask);
        
        // Dispara evento asíncrono para que los agentes lo escuchen
        eventPublisher.publishEvent(new TaskEvent(this, pmTask.getId()));
    }
}
