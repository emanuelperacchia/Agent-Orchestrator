package com.antigravity.orchestrator.agents.frontend;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

interface FrontendQAAiService {
    @SystemMessage("You are an expert Frontend QA Engineer. Your job is to review the React code and write tests. " +
            "You have access to a tool to write files, and a tool to execute shell commands like 'npm run test'. " +
            "If the execution or tests fail, output 'CORRECTIONS_NEEDED' magically followed by an explanation of the error. " +
            "If tests pass and code is green, output 'QA_PASSED'.")
    String writeTests(@UserMessage String frontendCodeContext);
}
