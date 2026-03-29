package com.antigravity.orchestrator.agents.frontend;

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
public class UXUIAgent implements Agent {
    private final UXUIAiService aiService;
    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UXUIAgent(ChatLanguageModel chatLanguageModel, TaskRepository taskRepository, ApplicationEventPublisher eventPublisher) {
        this.aiService = AiServices.builder(UXUIAiService.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
        this.taskRepository = taskRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Role getRole() { return Role.UX_UI_ARCHITECT; }

    @Override
    public String executeTask(Task task, String context) {
        return aiService.designUI("Context/Specs: " + context + "\nUX Task: " + task.getDescription());
    }

    @EventListener
    @Async
    public void onTaskEvent(TaskEvent event) {
        taskRepository.findById(event.getTaskId()).ifPresent(task -> {
            if (task.getAssignedTo() == getRole() && "PENDING".equals(task.getStatus())) {
                log.info("UX/UI picked up task #{}", task.getId());
                task.setStatus("IN_PROGRESS");
                taskRepository.save(task);

                try {
                    String result = executeTask(task, task.getExpectedOutput());
                    task.setExpectedOutput(result);
                    task.setStatus("DONE");
                    taskRepository.save(task);

                    log.info("UX/UI finished. Spawning Frontend Dev Task.");

                    Task feTask = Task.builder()
                            .description("Generate React/Vite/Tailwind frontend code based on UX design.")
                            .assignedTo(Role.FRONTEND_DEVELOPER)
                            .expectedOutput(result)
                            .status("PENDING")
                            .build();
                    feTask = taskRepository.save(feTask);
                    eventPublisher.publishEvent(new TaskEvent(this, feTask.getId()));

                } catch (Exception e) {
                    log.error("UX/UI Execution failed", e);
                    task.setStatus("FAILED");
                    taskRepository.save(task);
                }
            }
        });
    }
}
