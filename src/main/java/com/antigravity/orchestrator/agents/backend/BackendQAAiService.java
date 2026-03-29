package com.antigravity.orchestrator.agents.backend;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

interface BackendQAAiService {
    @SystemMessage("You are an expert Java QA Engineer. Your job is to review the code context and write JUnit 5 tests. " +
            "You have access to a tool to write files, and a tool to execute shell commands like 'mvn clean test'. " +
            "If the compilation or tests fail, output 'CORRECTIONS_NEEDED' magically followed by an explanation of the error. " +
            "If tests pass and code is green, output 'QA_PASSED'.")
    String writeTests(@UserMessage String generatedCodeContext);
}
