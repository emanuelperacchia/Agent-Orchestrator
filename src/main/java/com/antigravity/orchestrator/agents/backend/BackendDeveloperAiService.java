package com.antigravity.orchestrator.agents.backend;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

interface BackendDeveloperAiService {
    @SystemMessage("You are an expert Java Spring Boot developer. " +
            "Your job is to read a task description and write the corresponding Java code. " +
            "You have access to a tool to write files to disk. Use it to save your code to the appropriate paths. " +
            "Output an explanation of what you did after writing the files.")
    String developCode(@UserMessage String taskDescription);
}
