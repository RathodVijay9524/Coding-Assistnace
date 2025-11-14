package com.vijay.config;

import com.vijay.manager.ChainOfThoughtPlannerAdvisor;
import com.vijay.manager.EnhancedContextBuilderAdvisor;
import com.vijay.manager.EnhancedSelfRefineAdvisor;
import com.vijay.manager.KnowledgeGraphAdvisor;
import com.vijay.manager.LocalQueryPlannerAdvisor;
import com.vijay.manager.MultiCriteriaJudgeAdvisor;
import com.vijay.manager.QueryPlannerAdvisor;
import com.vijay.manager.SmartQualityAdvisor;
import com.vijay.manager.ResponseSummarizerAdvisor;
import com.vijay.manager.SelfRefineEvaluationAdvisor;
import com.vijay.tools.AIAgentToolService;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.ai.openai.OpenAiEmbeddingModel;
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

    // Local Ollama Embedding Model (Token-Free!)
    @Bean
    @Primary
    public OllamaEmbeddingModel ollamaEmbeddingModel() {
        logger.info("🚀 Creating LOCAL Ollama Embedding Model (nomic-embed-text) - NO TOKENS USED!");
        return OllamaEmbeddingModel.builder()
                .withModel("nomic-embed-text:latest")  // Your 274MB local model
                .withBaseUrl("http://localhost:11434")
                .build();
    }

    // Code Understanding Vector Stores (Using Local Ollama!)
    @Bean
    @Qualifier("summaryVectorStore")
    public VectorStore summaryVectorStore(OllamaEmbeddingModel embeddingModel) {
        logger.info("Creating Summary Vector Store for code file summaries using LOCAL Ollama embeddings");
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    @Qualifier("chunkVectorStore") 
    public VectorStore chunkVectorStore(OllamaEmbeddingModel embeddingModel) {
        logger.info("Creating Chunk Vector Store for code chunks using LOCAL Ollama embeddings");
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    // LOCAL OLLAMA CLIENT (Token-Free!) 🚀

    @Bean(name = "ollamaChatClient")
    @Primary
    ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel,
                               ChatMemory chatMemory,
                               LocalQueryPlannerAdvisor localPlanner,
                               KnowledgeGraphAdvisor knowledgeGraphAdvisor,
                               ResponseSummarizerAdvisor summarizerAdvisor,
                               AIAgentToolService aiAgentToolService) {
        logger.info("🚀 Creating LOCAL OLLAMA Chat Client - NO TOKENS USED! Local Intelligence v3.1");
        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(
                    MessageChatMemoryAdvisor.builder(chatMemory).build(),  // Memory
                    localPlanner,              // Brain 0: Local Query Planner (order: 0) - NO TOKENS!
                    knowledgeGraphAdvisor,     // Knowledge Graph (order: 100)
                    summarizerAdvisor          // Brain 2: Response Summarizer (order: 500)
                )
                .defaultTools(aiAgentToolService)  // Tools available for Brain 1
                .build();
    }

    // OpenAI client (backup for complex reasoning when needed)
    @Bean(name = "openAiChatClient")
    ChatClient openAiChatClient(OpenAiChatModel openAiChatModel,
                                ChatMemory chatMemory,
                                ChainOfThoughtPlannerAdvisor chainOfThoughtPlanner,
                                KnowledgeGraphAdvisor knowledgeGraphAdvisor,
                                ResponseSummarizerAdvisor summarizerAdvisor,
                                MultiCriteriaJudgeAdvisor multiCriteriaJudge,
                                AIAgentToolService aiAgentToolService) {
        logger.info("Creating OpenAI Chat Client (Backup) with Human-Like Thinking Architecture v3.0");
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(
                    MessageChatMemoryAdvisor.builder(chatMemory).build(),  // Memory
                    chainOfThoughtPlanner,     // Brain 0: Chain-of-Thought Planner (order: 0) - THINKS FIRST
                    knowledgeGraphAdvisor,     // Knowledge Graph (order: 100)
                    summarizerAdvisor,         // Brain 2: Response Summarizer (order: 500)
                    multiCriteriaJudge         // Brain 3: Multi-Criteria Judge (order: 1000) - VALIDATES LAST
                )
                .defaultTools(aiAgentToolService)  // Tools available for Brain 1
                .build();
    }

    @Bean(name = "anthropicChatClient")
    ChatClient anthropicChatClient(AnthropicChatModel anthropicChatModel,
                                   ChatMemory chatMemory,
                                   KnowledgeGraphAdvisor knowledgeGraphAdvisor,
                                   ResponseSummarizerAdvisor summarizerAdvisor,
                                   SelfRefineEvaluationAdvisor refineAdvisor,
                                   AIAgentToolService aiAgentToolService) {
        logger.info("Creating Anthropic Chat Client with MCP tools");
        return ChatClient.builder(anthropicChatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),  // Memory
                        knowledgeGraphAdvisor,     // Brain 0: Knowledge Graph (order: 100)
                        summarizerAdvisor,         // Brain 2: Summarizer (order: 500)
                        refineAdvisor              // Brain 3: Reasoner (order: 1000)
                )
                .defaultTools(aiAgentToolService)
                .build();
    }

    @Bean(name = "googleChatClient")
    ChatClient geminChatClient(GoogleGenAiChatModel googleGenAiChatModel,
                               ChatMemory chatMemory,
                               KnowledgeGraphAdvisor knowledgeGraphAdvisor,
                               ResponseSummarizerAdvisor summarizerAdvisor,
                               SelfRefineEvaluationAdvisor refineAdvisor,
                               AIAgentToolService aiAgentToolService) {
        logger.info("Creating google Chat Client with MCP tools");
        return ChatClient.builder(googleGenAiChatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),  // Memory
                        knowledgeGraphAdvisor,     // Brain 0: Knowledge Graph (order: 100)
                        summarizerAdvisor,         // Brain 2: Summarizer (order: 500)
                        refineAdvisor              // Brain 3: Reasoner (order: 1000)
                )
                .defaultTools(aiAgentToolService)
                .build();
    }

    @Bean(name = "ollamaChatClient")
    ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel,
                                 ChatMemory chatMemory,
                                KnowledgeGraphAdvisor knowledgeGraphAdvisor,
                                ResponseSummarizerAdvisor summarizerAdvisor,
                                SelfRefineEvaluationAdvisor refineAdvisor,
                                AIAgentToolService aiAgentToolService) {
        logger.info("Creating Ollama Chat Client with MCP tools");
        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),  // Memory
                        knowledgeGraphAdvisor,     // Brain 0: Knowledge Graph (order: 100)
                        summarizerAdvisor,         // Brain 2: Summarizer (order: 500)
                        refineAdvisor              // Brain 3: Reasoner (order: 1000)
                )
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
