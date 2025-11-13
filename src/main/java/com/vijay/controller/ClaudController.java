package com.vijay.controller;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ClaudController {

    private final AnthropicChatModel chatModel;

    public ClaudController(AnthropicChatModel chatModel) {
        this.chatModel = chatModel;
    }


    @GetMapping("/ai/claud")
    public Map generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of("generation", this.chatModel.call(message));
    }
}
