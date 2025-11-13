package com.vijay.config;

import com.vijay.tools.AIAgentToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.huggingface.HuggingfaceChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AIProviderConfig {

    private static final Logger logger = LoggerFactory.getLogger(AIProviderConfig.class);

    // Chat Memory for conversation context
    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
    }

    @Bean
    public ToolCallingManager toolCallingManager() {
        System.out.println("tool callback working");
        return ToolCallingManager.builder()
                .toolExecutionExceptionProcessor((toolName) -> {
                    System.out.println("--- TOOL EXECUTION FAILED: " + toolName + " ---");
                    return "Tool execution failed: ";
                })

                .build();
    }

    // OpenAI client with MCP tools

    @Bean(name = "openAiChatClient")
    ChatClient openAiChatClient(OpenAiChatModel openAiChatModel,
                                ChatMemory chatMemory,
                                AIAgentToolService aiAgentToolService) {
        logger.info("Creating OpenAI Chat Client with MCP tools");
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(aiAgentToolService)
                .build();
    }

    @Bean(name = "anthropicChatClient")
    ChatClient anthropicChatClient(AnthropicChatModel anthropicChatModel,
                                   ChatMemory chatMemory,
                                   AIAgentToolService aiAgentToolService) {
        logger.info("Creating Anthropic Chat Client with MCP tools");
        return ChatClient.builder(anthropicChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(aiAgentToolService)
                .build();
    }

    @Bean(name = "googleChatClient")
    ChatClient geminChatClient(GoogleGenAiChatModel googleGenAiChatModel,
                               ChatMemory chatMemory,
                               AIAgentToolService aiAgentToolService) {
        logger.info("Creating google Chat Client with MCP tools");
        return ChatClient.builder(googleGenAiChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(aiAgentToolService)
                .build();
    }

    @Bean(name = "ollamaChatClient")
    ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel,
                                 ChatMemory chatMemory,
                                AIAgentToolService aiAgentToolService) {
        logger.info("Creating Ollama Chat Client with MCP tools");
        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(aiAgentToolService)
                .build();
    }

    @Bean(name = "haggingFaceChatClient")
    ChatClient huggingfaceChatClient(HuggingfaceChatModel huggingfaceChatModel,
                                ChatMemory chatMemory,
                                     AIAgentToolService aiAgentToolService) {
        logger.info("Creating HaggingFace Chat Client with MCP tools");
        return ChatClient.builder(huggingfaceChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(aiAgentToolService)
                .build();
    }


}
