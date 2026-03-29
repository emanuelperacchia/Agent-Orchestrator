package com.antigravity.orchestrator.agents.frontend;

import com.antigravity.orchestrator.core.Agent;
import com.antigravity.orchestrator.core.Role;
import com.antigravity.orchestrator.core.Task;
import com.antigravity.orchestrator.events.TaskEvent;
import com.antigravity.orchestrator.repository.TaskRepository;
import com.antigravity.orchestrator.tools.CommandLineExecutionTool;
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
public class FrontendQAAgent implements Agent {
    private final FrontendQAAiService aiService;
    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;

    public FrontendQAAgent(ChatLanguageModel chatLanguageModel, FileWritingTool fileWritingTool, CommandLineExecutionTool commandLineTool, TaskRepository taskRepository, ApplicationEventPublisher eventPublisher) {
        this.aiService = AiServices.builder(FrontendQAAiService.class)
                .chatLanguageModel(chatLanguageModel)
                .tools(fileWritingTool, commandLineTool)
                .build();
        this.taskRepository = taskRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Role getRole() { return Role.FRONTEND_QA; }

    @Override
    public String executeTask(Task task, String context) {
        return aiService.writeTests("Frontend Code context: " + context + "\nTask: " + task.getDescription());
    }

    @EventListener
    @Async
    public void onTaskEvent(TaskEvent event) {
        taskRepository.findById(event.getTaskId()).ifPresent(task -> {
            if (task.getAssignedTo() == getRole() && "PENDING".equals(task.getStatus())) {
                log.info("Frontend QA picked up task #{}", task.getId());
                task.setStatus("IN_PROGRESS");
                taskRepository.save(task);

                try {
                    String result = executeTask(task, task.getExpectedOutput());
                    
                    if (result.contains("CORRECTIONS_NEEDED")) {
                        log.info("Frontend QA found errors! Triggering Reflection Loop back to Frontend Dev.");
                        Task devTask = Task.builder()
                                .description("CRITICAL QA FEEDBACK: The code failed to compile or pass tests. Fix errors:\n" + result)
                                .assignedTo(Role.FRONTEND_DEVELOPER)
                                .expectedOutput(task.getExpectedOutput())
                                .status("PENDING")
                                .build();
                        devTask = taskRepository.save(devTask);
                        eventPublisher.publishEvent(new TaskEvent(this, devTask.getId()));
                        
                        task.setStatus("FAILED");
                    } else {
                        task.setExpectedOutput(result);
                        task.setStatus("DONE");
                        log.info("Frontend QA finished task #{}. Code is green. Frontend pipeline complete!!!", task.getId());
                    }
                    taskRepository.save(task);
                } catch (Exception e) {
                    log.error("Frontend QA Execution failed", e);
                    task.setStatus("FAILED");
                    taskRepository.save(task);
                }
            }
        });
    }
}
