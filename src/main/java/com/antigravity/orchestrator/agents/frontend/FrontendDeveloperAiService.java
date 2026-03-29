package com.antigravity.orchestrator.agents.frontend;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

interface FrontendDeveloperAiService {
    @SystemMessage("You are an expert React/Vite/Tailwind Frontend Developer. " +
            "Read the UX design and generate React .tsx components. " +
            "You have access to a tool to write files to disk. Save your generated code into a frontend project directory. " +
            "Output an explanation of what you did after writing the files.")
    String developFrontend(@UserMessage String uxDesign);
}
