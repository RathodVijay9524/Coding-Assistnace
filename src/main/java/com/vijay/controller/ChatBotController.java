package com.vijay.controller;

import com.vijay.dto.ChatRequest;
import com.vijay.dto.ChatResponse;
import com.vijay.service.ChatService;
import com.vijay.context.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
public class ChatBotController {

    private static final Logger logger = LoggerFactory.getLogger(ChatBotController.class);
    
    private final ChatService chatService;

    @Autowired
    public ChatBotController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/")
    public String indexPage() {
        return "index";
    }

    @GetMapping("/chatbot")
    public String chatbotPage(Model model) {
        model.addAttribute("providers", chatService.getSupportedProviders());
        return "chatbot";
    }

    @PostMapping("/send")
    @ResponseBody
    public ResponseEntity<ChatResponse> sendMessage(@RequestParam String message,
                                                   @RequestParam String provider,
                                                   @RequestParam(defaultValue = "true") boolean useTools) {
        // Initialize TraceContext for request tracing
        String traceId = UUID.randomUUID().toString();
        TraceContext.initialize(traceId);
        logger.info("🔍 TraceContext initialized: {} | Message: {} | Provider: {}", traceId, message, provider);
        
        try {
            ChatRequest request = new ChatRequest(message, useTools);
            ChatResponse response = chatService.processChat(provider, request);
            logger.info("✅ Request completed successfully (traceId: {})", traceId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error processing chatbot message (traceId: {}): {}", traceId, e.getMessage(), e);
            ChatResponse errorResponse = new ChatResponse("Sorry, I encountered an error: " + e.getMessage(), provider);
            return ResponseEntity.status(500).body(errorResponse);
        } finally {
            // Clear TraceContext after request completes
            TraceContext.clear();
            logger.info("🧹 TraceContext cleared (traceId: {})", traceId);
        }
    }
}
