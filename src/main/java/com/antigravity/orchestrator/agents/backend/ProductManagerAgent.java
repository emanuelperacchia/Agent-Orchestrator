package com.antigravity.orchestrator.agents.backend;

import com.antigravity.orchestrator.core.Agent;
import com.antigravity.orchestrator.core.Role;
import com.antigravity.orchestrator.core.Task;
import com.antigravity.orchestrator.events.TaskEvent;
import com.antigravity.orchestrator.repository.TaskRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductManagerAgent implements Agent {

    private final ProductManagerAiService aiService;
    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProductManagerAgent(ChatLanguageModel chatLanguageModel, TaskRepository taskRepository, ApplicationEventPublisher eventPublisher) {
        this.aiService = AiServices.builder(ProductManagerAiService.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
        this.taskRepository = taskRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Role getRole() {
        return Role.PRODUCT_MANAGER;
    }

    @Override
    public String executeTask(Task task, String context) {
        return aiService.breakdownTasks("Context: " + context + "\nRequirement: " + task.getDescription());
    }

    @EventListener
    @Async
    public void onTaskEvent(TaskEvent event) {
        taskRepository.findById(event.getTaskId()).ifPresent(task -> {
            if (task.getAssignedTo() == getRole() && "PENDING".equals(task.getStatus())) {
                log.info("PM picked up task #{}", task.getId());
                task.setStatus("IN_PROGRESS");
                taskRepository.save(task);

                try {
                    String specs = executeTask(task, "Start of Project");
                    task.setExpectedOutput(specs);
                    task.setStatus("DONE");
                    taskRepository.save(task);

                    log.info("PM generated architecture. Spawning Backend and UX/UI tasks in parallel.");

                    Task backendTask = Task.builder()
                            .description("Generate Spring Boot components based on specs")
                            .assignedTo(Role.BACKEND_DEVELOPER)
                            .expectedOutput(specs)
                            .status("PENDING")
                            .build();
                    backendTask = taskRepository.save(backendTask);
                    eventPublisher.publishEvent(new TaskEvent(this, backendTask.getId()));

                    Task uxTask = Task.builder()
                            .description("Design React UI based on backend specs")
                            .assignedTo(Role.UX_UI_ARCHITECT)
                            .expectedOutput(specs)
                            .status("PENDING")
                            .build();
                    uxTask = taskRepository.save(uxTask);
                    eventPublisher.publishEvent(new TaskEvent(this, uxTask.getId()));

                } catch (Exception e) {
                    log.error("PM Execution failed", e);
                    task.setStatus("FAILED");
                    taskRepository.save(task);
                }
            }
        });
    }
}
