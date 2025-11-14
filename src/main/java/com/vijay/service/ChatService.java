package com.vijay.service;

import com.vijay.dto.ChatRequest;
import com.vijay.dto.ChatResponse;
import com.vijay.tools.ToolFinderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient openAiChatClient;
    private final ChatClient anthropicChatClient;
    private final ChatClient googleChatClient;
    private final ChatClient ollamaChatClient;
    private final ChatClient huggingFaceChatClient;
    private final ToolFinderService toolFinderService;
    private final CodeRetrieverService codeRetrieverService;

    public ChatService(@Qualifier("openAiChatClient") ChatClient openAiChatClient,
                      @Qualifier("anthropicChatClient") ChatClient anthropicChatClient,
                      @Qualifier("googleChatClient") ChatClient googleChatClient,
                      @Qualifier("ollamaChatClient") ChatClient ollamaChatClient,
                      @Qualifier("haggingFaceChatClient") ChatClient huggingFaceChatClient,
                      ToolFinderService toolFinderService,
                      CodeRetrieverService codeRetrieverService) {
        this.openAiChatClient = openAiChatClient;
        this.anthropicChatClient = anthropicChatClient;
        this.googleChatClient = googleChatClient;
        this.ollamaChatClient = ollamaChatClient;
        this.huggingFaceChatClient = huggingFaceChatClient;
        this.toolFinderService = toolFinderService;
        this.codeRetrieverService = codeRetrieverService;
    }

    public ChatResponse processChat(String provider, ChatRequest request) {
        logger.info("Processing chat request for provider: {} with message: {}", provider, request.getMessage());

        try {
            ChatClient chatClient = getChatClientForProvider(provider);
            String response;
            String[] toolsUsed = new String[0];

            if (request.isUseTools()) {
                // Smart brain activation based on query intent
                String codeContext = "";
                List<String> requiredToolNames = new ArrayList<>();
                
                // Determine query intent (this will be set by QueryPlannerAdvisor)
                String queryIntent = getQueryIntent(request.getMessage());
                logger.info("🎯 Query Intent: {}", queryIntent);
                
                switch (queryIntent) {
                    case "CODE":
                        logger.info("🧠 Activating Brain 1 (Code Retriever) only");
                        CodeRetrieverService.CodeContext context = codeRetrieverService.retrieveCodeContext(request.getMessage());
                        if (!context.isEmpty()) {
                            codeContext = "\n\n📋 **Code Context from your codebase:**\n" + context.getFormattedContext();
                        }
                        // No tools needed for code queries
                        break;
                        
                    case "TOOLS":
                        logger.info("🧠 Activating Brain 0 (Tool Finder) only");
                        requiredToolNames = toolFinderService.findToolsFor(request.getMessage());
                        logger.info("Tools activated for provider {}: {}", provider, requiredToolNames);
                        // No code context needed for tool queries
                        break;
                        
                    case "GENERAL":
                        logger.info("🧠 General chat mode - no special brains activated");
                        // Neither tools nor code context needed
                        break;
                        
                    default:
                        // Fallback to old behavior if intent detection fails
                        logger.info("🧠 Fallback: Activating both brains");
                        requiredToolNames = toolFinderService.findToolsFor(request.getMessage());
                        if (isCodeRelatedQuery(request.getMessage())) {
                            CodeRetrieverService.CodeContext fallbackContext = codeRetrieverService.retrieveCodeContext(request.getMessage());
                            if (!fallbackContext.isEmpty()) {
                                codeContext = "\n\n📋 **Code Context from your codebase:**\n" + fallbackContext.getFormattedContext();
                            }
                        }
                        break;
                }
                
                toolsUsed = requiredToolNames.toArray(new String[0]);
                
                // Build the enhanced prompt with appropriate context
                String enhancedMessage = request.getMessage() + codeContext;
                
                // Since ChatClients from AIProviderConfig already have default tools,
                // we can either use them directly or add specific tool names
                if (toolsUsed.length > 0) {
                    response = chatClient.prompt()
                            .user(enhancedMessage)
                            .toolNames(toolsUsed)
                            .call()
                            .content();
                } else {
                    // Use default tools configured in AIProviderConfig
                    response = chatClient.prompt()
                            .user(enhancedMessage)
                            .call()
                            .content();
                }
            } else {
                // Simple chat without tools - create a new client without tools
                response = chatClient.prompt()
                        .user(request.getMessage())
                        .call()
                        .content();
            }

            logger.info("Response generated for provider {}: {}", provider, response);
            return new ChatResponse(response, provider, toolsUsed);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid provider: {}", provider);
            throw e;
        } catch (Exception e) {
            logger.error("Error processing chat request for provider {}: {}", provider, e.getMessage());
            throw new RuntimeException("Error processing request: " + e.getMessage(), e);
        }
    }

    private ChatClient getChatClientForProvider(String provider) {
        switch (provider.toLowerCase()) {
            case "openai":
                return openAiChatClient;
            case "claude":
            case "anthropic":
                return anthropicChatClient;
            case "google":
            case "gemini":
                return googleChatClient;
            case "ollama":
                return ollamaChatClient;
            case "huggingface":
            case "hf":
                return huggingFaceChatClient;
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider + 
                    ". Supported providers: openai, claude/anthropic, google/gemini, ollama, huggingface/hf");
        }
    }

    public String[] getSupportedProviders() {
        return new String[]{"openai", "claude", "anthropic", "google", "gemini", "ollama", "huggingface", "hf"};
    }

    private boolean isCodeRelatedQuery(String message) {
        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("chatservice") ||
               lowerMessage.contains("aiproviderconfig") ||
               lowerMessage.contains("knowledgegraphadvisor") ||
               lowerMessage.contains("responsesummarizeradvisor") ||
               lowerMessage.contains("selfrefine") ||
               lowerMessage.contains("toolfinderservice") ||
               lowerMessage.contains("coderetrieverservice") ||
               lowerMessage.contains("how does") ||
               lowerMessage.contains("show me") ||
               lowerMessage.contains("explain") ||
               lowerMessage.contains("what does") ||
               lowerMessage.contains("advisor") ||
               lowerMessage.contains("service") ||
               lowerMessage.contains("config") ||
               lowerMessage.contains("architecture") ||
               lowerMessage.contains("dependency") ||
               lowerMessage.contains("brain") ||
               lowerMessage.contains("implementation") ||
               lowerMessage.contains("code") ||
               lowerMessage.contains("class") ||
               lowerMessage.contains("method") ||
               lowerMessage.contains("function");
    }

    private String getQueryIntent(String message) {
        // Simple intent detection - this will be enhanced by QueryPlannerAdvisor
        String lowerMessage = message.toLowerCase();
        
        // Code-related patterns
        if (lowerMessage.contains("chatservice") || lowerMessage.contains("aiproviderconfig") ||
            lowerMessage.contains("advisor") || lowerMessage.contains("how does") ||
            lowerMessage.contains("show me") || lowerMessage.contains("explain") ||
            lowerMessage.contains("architecture") || lowerMessage.contains("code") ||
            lowerMessage.contains("class") || lowerMessage.contains("method") ||
            lowerMessage.contains("service") || lowerMessage.contains("config")) {
            return "CODE";
        }
        
        // Tool-related patterns
        if (lowerMessage.contains("weather") || lowerMessage.contains("calendar") ||
            lowerMessage.contains("meeting") || lowerMessage.contains("schedule") ||
            lowerMessage.contains("search") || lowerMessage.contains("email") ||
            lowerMessage.contains("time") || lowerMessage.contains("date") ||
            lowerMessage.contains("forecast") || lowerMessage.contains("temperature")) {
            return "TOOLS";
        }
        
        // General conversation patterns
        if (lowerMessage.contains("hello") || lowerMessage.contains("hi") ||
            lowerMessage.contains("hey") || lowerMessage.contains("how are you") ||
            lowerMessage.contains("what can you do") || lowerMessage.contains("help")) {
            return "GENERAL";
        }
        
        // Default fallback
        return "UNKNOWN";
    }
}
