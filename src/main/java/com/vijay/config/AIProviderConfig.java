package com.vijay.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;

public class AIProviderConfig {

    private static final Logger logger = LoggerFactory.getLogger(AIProviderConfig.class);

    // Chat Memory for conversation context
    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
    }
  /*
    // OpenAI client with MCP tools
    @Bean(name = "openAiChatClient")
    ChatClient openAiChatClient(OpenAiChatModel openAiChatModel,
                                ChatMemory chatMemory) {
        logger.info("Creating OpenAI Chat Client with MCP tools");
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Bean(name = "anthropicChatClient")
    ChatClient anthropicChatClient(AnthropicChatModel anthropicChatModel,
                                   ChatMemory chatMemory) {
        logger.info("Creating Anthropic Chat Client with MCP tools");
        return ChatClient.builder(anthropicChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Bean(name = "ollamaChatClient")
    ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel,
                                 ChatMemory chatMemory) {
        logger.info("Creating Ollama Chat Client with MCP tools");
        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }*/

}
