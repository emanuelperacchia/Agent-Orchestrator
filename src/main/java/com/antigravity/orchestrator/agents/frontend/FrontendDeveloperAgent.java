package com.antigravity.orchestrator.agents.frontend;

import com.antigravity.orchestrator.core.Agent;
import com.antigravity.orchestrator.core.Role;
import com.antigravity.orchestrator.core.Task;
import com.antigravity.orchestrator.events.TaskEvent;
import com.antigravity.orchestrator.repository.TaskRepository;
import com.antigravity.orchestrator.tools.DirectoryListingTool;
import com.antigravity.orchestrator.tools.FileReadingTool;
import com.antigravity.orchestrator.tools.FileWritingTool;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FrontendDeveloperAgent implements Agent {
    private final FrontendDeveloperAiService aiService;
    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;

    public FrontendDeveloperAgent(
            ChatLanguageModel chatLanguageModel, 
            FileWritingTool fileWritingTool, 
            FileReadingTool fileReadingTool,
            DirectoryListingTool directoryListingTool,
            TaskRepository taskRepository, 
            ApplicationEventPublisher eventPublisher) {
            
        this.aiService = AiServices.builder(FrontendDeveloperAiService.class)
                .chatLanguageModel(chatLanguageModel)
                .tools(fileWritingTool, fileReadingTool, directoryListingTool)
                .build();
        this.taskRepository = taskRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Role getRole() { return Role.FRONTEND_DEVELOPER; }

    @Override
    public String executeTask(Task task, String context) {
        return aiService.developFrontend("Design Specs: " + context + "\nTask: " + task.getDescription());
    }

    @EventListener
    @Async
    public void onTaskEvent(TaskEvent event) {
        taskRepository.findById(event.getTaskId()).ifPresent(task -> {
            if (task.getAssignedTo() == getRole() && "PENDING".equals(task.getStatus())) {
                log.info("Frontend Dev picked up task #{}", task.getId());
                task.setStatus("IN_PROGRESS");
                taskRepository.save(task);

                try {
                    String result = executeTask(task, task.getExpectedOutput());
                    task.setExpectedOutput(result);
                    task.setStatus("DONE");
                    taskRepository.save(task);

                    log.info("Frontend Dev finished. Spawning Frontend QA Task.");

                    Task qaTask = Task.builder()
                            .description("Write Vitest tests for the frontend components.")
                            .assignedTo(Role.FRONTEND_QA)
                            .expectedOutput(task.getExpectedOutput() + "\n" + result)
                            .status("PENDING")
                            .build();
                    qaTask = taskRepository.save(qaTask);
                    eventPublisher.publishEvent(new TaskEvent(this, qaTask.getId()));

                } catch (Exception e) {
                    log.error("Frontend Dev Execution failed", e);
                    task.setStatus("FAILED");
                    taskRepository.save(task);
                }
            }
        });
    }
}
