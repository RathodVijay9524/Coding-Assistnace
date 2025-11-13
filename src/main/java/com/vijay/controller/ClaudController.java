package com.vijay.controller;

import com.vijay.tools.ToolFinderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ClaudController {

    private static final Logger logger = LoggerFactory.getLogger(ClaudController.class);


    private ChatClient chatClient;
    private final ToolFinderService toolFinder;


    public ClaudController(AnthropicChatModel chatModel,ToolFinderService toolFinder){
        this.chatClient = ChatClient.create(chatModel);
        this.toolFinder = toolFinder;
    }

    @GetMapping("/ai/claud")
    public String chat(@RequestParam String prompt) {
        logger.info("Chat request received: {}", prompt);

        // 1. SMART FINDER (RAG) STEP:
        // Find the *names* of the tools needed (e.g., ["getWeather", "sendEmail"])
        List<String> requiredToolNames = toolFinder.findToolsFor(prompt);
        logger.info("SmartFinder: Activating tools for this request: {}", requiredToolNames);

        // Convert the List<String> to a String[] for the .toolNames() method
        String[] toolNamesArray = requiredToolNames.toArray(new String[0]);

        // 2. "AGENTIC" CALL:
        // Use the single, pre-built chatClient.
        String content = this.chatClient.prompt()
                .user(prompt)
                .toolNames(toolNamesArray)
                .call()
                .content();

        logger.info("Final AI response: {}", content);
        return content;
    }
}
