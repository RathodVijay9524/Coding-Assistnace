package com.vijay.controller;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.huggingface.HuggingfaceChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.Map;

@RestController
public class HaggingFaceCongroller {

    private final HuggingfaceChatModel chatModel;

    public HaggingFaceCongroller(HuggingfaceChatModel chatModel) {
        this.chatModel = chatModel;
    }


    @GetMapping("/ai/haggingface")
    public Map generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of("generation", this.chatModel.call(message));
    }
}
