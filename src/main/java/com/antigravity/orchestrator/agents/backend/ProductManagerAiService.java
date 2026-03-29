package com.antigravity.orchestrator.agents.backend;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

interface ProductManagerAiService {
    @SystemMessage("You are an expert Product Manager and Software Architect. " +
            "Your job is to take a user requirement and output a detailed JSON array of tasks for a Spring Boot backend developer. " +
            "Return ONLY raw JSON, do not use markdown blocks like ```json.")
    String breakdownTasks(@UserMessage String userRequirement);
}
