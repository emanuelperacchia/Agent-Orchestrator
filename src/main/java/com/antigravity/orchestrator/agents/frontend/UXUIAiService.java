package com.antigravity.orchestrator.agents.frontend;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

interface UXUIAiService {
    @SystemMessage("You are an expert UX/UI Architect. " +
            "Your job is to read backend specifications and user requirements to design a React component tree and Tailwind CSS styling plan. " +
            "Output your design specs in structured markdown so the Frontend Developer can implement it.")
    String designUI(@UserMessage String specifications);
}
