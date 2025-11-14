package com.vijay.config;

import com.vijay.manager.AdvancedCapabilitiesAdvisor;
import com.vijay.manager.ChainOfThoughtPlannerAdvisor;
import com.vijay.manager.CognitiveBiasAdvisor;
import com.vijay.manager.ConversationMemoryAdvisor;
import com.vijay.manager.EmotionalContextAdvisor;
import com.vijay.manager.EmotionalResponseAdvisor;
import com.vijay.manager.EnhancedContextBuilderAdvisor;
import com.vijay.manager.LearningGrowthAdvisor;
import com.vijay.manager.PersonalityAdvisor;
import com.vijay.manager.TheoryOfMindAdvisor;
import com.vijay.manager.ThoughtStreamAdvisor;
import com.vijay.manager.EnhancedSelfRefineAdvisor;
import com.vijay.manager.ErrorPredictionAdvisor;
import com.vijay.manager.KnowledgeGraphAdvisor;
import com.vijay.manager.LearningSystemAdvisor;
import com.vijay.manager.LocalQueryPlannerAdvisor;
import com.vijay.manager.MultiCriteriaJudgeAdvisor;
import com.vijay.manager.QueryPlannerAdvisor;
import com.vijay.manager.SmartQualityAdvisor;
import com.vijay.manager.ResponseSummarizerAdvisor;
import com.vijay.manager.SelfRefineEvaluationAdvisor;
import com.vijay.manager.SelfRefineV3Advisor;
import com.vijay.manager.UserProfilingAdvisor;
import com.vijay.service.FileHashTracker;
import com.vijay.service.IncrementalIndexer;
import com.vijay.service.IncrementalSummarizer;
import com.vijay.service.IncrementalGraphCalculator;
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

    // Code Understanding Vector Stores (Using Local Ollama!)
    // Note: OllamaEmbeddingModel will be auto-configured by Spring AI from application.properties
    @Bean
    @Qualifier("summaryVectorStore")
    public VectorStore summaryVectorStore(OllamaEmbeddingModel embeddingModel) {
        logger.info("🚀 Creating Summary Vector Store using LOCAL Ollama embeddings (nomic-embed-text) - NO TOKENS USED!");
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    @Qualifier("chunkVectorStore") 
    public VectorStore chunkVectorStore(OllamaEmbeddingModel embeddingModel) {
        logger.info("🚀 Creating Chunk Vector Store using LOCAL Ollama embeddings (nomic-embed-text) - NO TOKENS USED!");
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    // LOCAL OLLAMA CLIENT (Token-Free!) 🚀

    @Bean(name = "ollamaChatClient")
    @Primary
    ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel,
                               ChatMemory chatMemory,
                               ThoughtStreamAdvisor thoughtStreamAdvisor,
                               EmotionalContextAdvisor emotionalContextAdvisor,
                               EmotionalResponseAdvisor emotionalResponseAdvisor,
                               TheoryOfMindAdvisor theoryOfMindAdvisor,
                               PersonalityAdvisor personalityAdvisor,
                               CognitiveBiasAdvisor cognitiveBiasAdvisor,
                               AdvancedCapabilitiesAdvisor advancedCapabilitiesAdvisor,
                               LearningGrowthAdvisor learningGrowthAdvisor,
                               ConversationMemoryAdvisor conversationMemory,
                               UserProfilingAdvisor userProfilingAdvisor,
                               LocalQueryPlannerAdvisor localPlanner,
                               ErrorPredictionAdvisor errorPredictionAdvisor,
                               KnowledgeGraphAdvisor knowledgeGraphAdvisor,
                               LearningSystemAdvisor learningSystemAdvisor,
                               ResponseSummarizerAdvisor summarizerAdvisor,
                               SelfRefineV3Advisor selfRefineV3Advisor,
                               AIAgentToolService aiAgentToolService) {
        logger.info("🚀 Creating LOCAL OLLAMA Chat Client - Multi-Brain Architecture v7.0 (Supervisor Brain + Self-Refine V3)");
        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(
                    thoughtStreamAdvisor,      // Brain -1: Thought Stream (order: -1) ⭐ Phase 6
                    localPlanner,              // Brain 0: Local Query Planner (order: 0)
                    emotionalContextAdvisor,   // Brain 7: Emotional Context (order: 1) ⭐ Phase 3
                    conversationMemory,        // Brain Memory: Conversation Context (order: 1)
                    MessageChatMemoryAdvisor.builder(chatMemory).build(),  // Short-term Memory
                    theoryOfMindAdvisor,       // Brain 8: Theory of Mind (order: 3) ⭐ Phase 3
                    userProfilingAdvisor,      // Brain 5: User Profiling (order: 2)
                    errorPredictionAdvisor,    // Brain 6: Error Prediction (order: 5)
                    knowledgeGraphAdvisor,     // Knowledge Graph (order: 100)
                    learningSystemAdvisor,     // Brain 4: Learning System (order: 7)
                    summarizerAdvisor,         // Brain 2: Response Summarizer (order: 500)
                    emotionalResponseAdvisor,  // Brain 7: Emotional Response (order: 750) ⭐ Phase 3
                    personalityAdvisor,        // Brain 9: Personality (order: 800) ⭐ Phase 3
                    cognitiveBiasAdvisor,      // Brain 10: Cognitive Bias (order: 850) ⭐ Phase 4
                    advancedCapabilitiesAdvisor, // Brain 11: Advanced Capabilities (order: 900) ⭐ Phase 4
                    learningGrowthAdvisor,     // Brain 12: Learning & Growth (order: 950) ⭐ Phase 5
                    selfRefineV3Advisor        // Brain 13: Self-Refine V3 (order: 1000) ⭐ Phase 7
                )
                .defaultTools(aiAgentToolService)  // Tools available for Brain 1
                .build();
    }

    // OpenAI client (backup for complex reasoning when needed)
    @Bean(name = "openAiChatClient")
    ChatClient openAiChatClient(OpenAiChatModel openAiChatModel,
                                ChatMemory chatMemory,
                                ThoughtStreamAdvisor thoughtStreamAdvisor,
                                EmotionalContextAdvisor emotionalContextAdvisor,
                                EmotionalResponseAdvisor emotionalResponseAdvisor,
                                TheoryOfMindAdvisor theoryOfMindAdvisor,
                                PersonalityAdvisor personalityAdvisor,
                                CognitiveBiasAdvisor cognitiveBiasAdvisor,
                                AdvancedCapabilitiesAdvisor advancedCapabilitiesAdvisor,
                                LearningGrowthAdvisor learningGrowthAdvisor,
                                ConversationMemoryAdvisor conversationMemory,
                                UserProfilingAdvisor userProfilingAdvisor,
                                ChainOfThoughtPlannerAdvisor chainOfThoughtPlanner,
                                ErrorPredictionAdvisor errorPredictionAdvisor,
                                KnowledgeGraphAdvisor knowledgeGraphAdvisor,
                                LearningSystemAdvisor learningSystemAdvisor,
                                ResponseSummarizerAdvisor summarizerAdvisor,
                                SelfRefineV3Advisor selfRefineV3Advisor,
                                AIAgentToolService aiAgentToolService) {
        logger.info("🧠 Creating OpenAI Chat Client - Multi-Brain Architecture v7.0 (Supervisor Brain + Self-Refine V3)");
        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(
                    thoughtStreamAdvisor,      // Brain -1: Thought Stream (order: -1) ⭐ Phase 6
                    chainOfThoughtPlanner,     // Brain 0: Chain-of-Thought Planner (order: 0)
                    emotionalContextAdvisor,   // Brain 7: Emotional Context (order: 1) ⭐ Phase 3
                    conversationMemory,        // Brain Memory: Conversation Context (order: 1)
                    MessageChatMemoryAdvisor.builder(chatMemory).build(),  // Short-term Memory
                    theoryOfMindAdvisor,       // Brain 8: Theory of Mind (order: 3) ⭐ Phase 3
                    userProfilingAdvisor,      // Brain 5: User Profiling (order: 2)
                    errorPredictionAdvisor,    // Brain 6: Error Prediction (order: 5)
                    knowledgeGraphAdvisor,     // Knowledge Graph (order: 100)
                    learningSystemAdvisor,     // Brain 4: Learning System (order: 7)
                    summarizerAdvisor,         // Brain 2: Response Summarizer (order: 500)
                    emotionalResponseAdvisor,  // Brain 7: Emotional Response (order: 750) ⭐ Phase 3
                    personalityAdvisor,        // Brain 9: Personality (order: 800) ⭐ Phase 3
                    cognitiveBiasAdvisor,      // Brain 10: Cognitive Bias (order: 850) ⭐ Phase 4
                    advancedCapabilitiesAdvisor, // Brain 11: Advanced Capabilities (order: 900) ⭐ Phase 4
                    learningGrowthAdvisor,     // Brain 12: Learning & Growth (order: 950) ⭐ Phase 5
                    selfRefineV3Advisor        // Brain 13: Self-Refine V3 (order: 1000) ⭐ Phase 7
                )
                .defaultTools(aiAgentToolService)  // Tools available for Brain 1
                .build();
    }

    @Bean(name = "anthropicChatClient")
    ChatClient anthropicChatClient(AnthropicChatModel anthropicChatModel,
                                   ChatMemory chatMemory,
                                   ThoughtStreamAdvisor thoughtStreamAdvisor,
                                   EmotionalContextAdvisor emotionalContextAdvisor,
                                   EmotionalResponseAdvisor emotionalResponseAdvisor,
                                   TheoryOfMindAdvisor theoryOfMindAdvisor,
                                   PersonalityAdvisor personalityAdvisor,
                                   CognitiveBiasAdvisor cognitiveBiasAdvisor,
                                   AdvancedCapabilitiesAdvisor advancedCapabilitiesAdvisor,
                                   LearningGrowthAdvisor learningGrowthAdvisor,
                                   ConversationMemoryAdvisor conversationMemory,
                                   UserProfilingAdvisor userProfilingAdvisor,
                                   ChainOfThoughtPlannerAdvisor chainOfThoughtPlanner,
                                   ErrorPredictionAdvisor errorPredictionAdvisor,
                                   KnowledgeGraphAdvisor knowledgeGraphAdvisor,
                                   LearningSystemAdvisor learningSystemAdvisor,
                                   ResponseSummarizerAdvisor summarizerAdvisor,
                                   SelfRefineV3Advisor selfRefineV3Advisor,
                                   AIAgentToolService aiAgentToolService) {
        logger.info("Creating Anthropic Chat Client with MCP tools");
        return ChatClient.builder(anthropicChatModel)
                .defaultAdvisors(
                        thoughtStreamAdvisor,      // Brain -1: Thought Stream (order: -1) ⭐ Phase 6
                        chainOfThoughtPlanner,     // Brain 0: Chain-of-Thought Planner (order: 0)
                        emotionalContextAdvisor,   // Brain 7: Emotional Context (order: 1) ⭐ Phase 3
                        conversationMemory,        // Brain Memory: Conversation Context (order: 1)
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),  // Short-term Memory
                        theoryOfMindAdvisor,       // Brain 8: Theory of Mind (order: 3) ⭐ Phase 3
                        userProfilingAdvisor,      // Brain 5: User Profiling (order: 2)
                        errorPredictionAdvisor,    // Brain 6: Error Prediction (order: 5)
                        knowledgeGraphAdvisor,     // Knowledge Graph (order: 100)
                        learningSystemAdvisor,     // Brain 4: Learning System (order: 7)
                        summarizerAdvisor,         // Brain 2: Response Summarizer (order: 500)
                        emotionalResponseAdvisor,  // Brain 7: Emotional Response (order: 750) ⭐ Phase 3
                        personalityAdvisor,        // Brain 9: Personality (order: 800) ⭐ Phase 3
                        cognitiveBiasAdvisor,      // Brain 10: Cognitive Bias (order: 850) ⭐ Phase 4
                        advancedCapabilitiesAdvisor, // Brain 11: Advanced Capabilities (order: 900) ⭐ Phase 4
                        learningGrowthAdvisor,     // Brain 12: Learning & Growth (order: 950) ⭐ Phase 5
                        selfRefineV3Advisor        // Brain 13: Self-Refine V3 (order: 1000) ⭐ Phase 7
                )
                .defaultTools(aiAgentToolService)
                .build();
    }

    @Bean(name = "googleChatClient")
    ChatClient geminChatClient(GoogleGenAiChatModel googleGenAiChatModel,
                               ChatMemory chatMemory,
                               ThoughtStreamAdvisor thoughtStreamAdvisor,
                               EmotionalContextAdvisor emotionalContextAdvisor,
                               EmotionalResponseAdvisor emotionalResponseAdvisor,
                               TheoryOfMindAdvisor theoryOfMindAdvisor,
                               PersonalityAdvisor personalityAdvisor,
                               CognitiveBiasAdvisor cognitiveBiasAdvisor,
                               AdvancedCapabilitiesAdvisor advancedCapabilitiesAdvisor,
                               LearningGrowthAdvisor learningGrowthAdvisor,
                               ConversationMemoryAdvisor conversationMemory,
                               UserProfilingAdvisor userProfilingAdvisor,
                               ChainOfThoughtPlannerAdvisor chainOfThoughtPlanner,
                               ErrorPredictionAdvisor errorPredictionAdvisor,
                               KnowledgeGraphAdvisor knowledgeGraphAdvisor,
                               LearningSystemAdvisor learningSystemAdvisor,
                               ResponseSummarizerAdvisor summarizerAdvisor,
                               SelfRefineV3Advisor selfRefineV3Advisor,
                               AIAgentToolService aiAgentToolService) {
        logger.info("Creating google Chat Client with MCP tools");
        return ChatClient.builder(googleGenAiChatModel)
                .defaultAdvisors(
                        thoughtStreamAdvisor,      // Brain -1: Thought Stream (order: -1) ⭐ Phase 6
                        chainOfThoughtPlanner,     // Brain 0: Chain-of-Thought Planner (order: 0)
                        emotionalContextAdvisor,   // Brain 7: Emotional Context (order: 1) ⭐ Phase 3
                        conversationMemory,        // Brain Memory: Conversation Context (order: 1)
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),  // Short-term Memory
                        theoryOfMindAdvisor,       // Brain 8: Theory of Mind (order: 3) ⭐ Phase 3
                        userProfilingAdvisor,      // Brain 5: User Profiling (order: 2)
                        errorPredictionAdvisor,    // Brain 6: Error Prediction (order: 5)
                        knowledgeGraphAdvisor,     // Knowledge Graph (order: 100)
                        learningSystemAdvisor,     // Brain 4: Learning System (order: 7)
                        summarizerAdvisor,         // Brain 2: Response Summarizer (order: 500)
                        emotionalResponseAdvisor,  // Brain 7: Emotional Response (order: 750) ⭐ Phase 3
                        personalityAdvisor,        // Brain 9: Personality (order: 800) ⭐ Phase 3
                        cognitiveBiasAdvisor,      // Brain 10: Cognitive Bias (order: 850) ⭐ Phase 4
                        advancedCapabilitiesAdvisor, // Brain 11: Advanced Capabilities (order: 900) ⭐ Phase 4
                        learningGrowthAdvisor,     // Brain 12: Learning & Growth (order: 950) ⭐ Phase 5
                        selfRefineV3Advisor        // Brain 13: Self-Refine V3 (order: 1000) ⭐ Phase 7
                )
                .defaultTools(aiAgentToolService)
                .build();
    }


    @Bean(name = "haggingFaceChatClient")
    ChatClient huggingfaceChatClient(HuggingfaceChatModel huggingfaceChatModel,
                                     ChatMemory chatMemory,
                                     KnowledgeGraphAdvisor knowledgeGraphAdvisor,
                                     ResponseSummarizerAdvisor summarizerAdvisor,
                                     SelfRefineEvaluationAdvisor refineAdvisor,
                                     AIAgentToolService aiAgentToolService) {
        logger.info("Creating HaggingFace Chat Client with MCP tools");
        return ChatClient.builder(huggingfaceChatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),  // Memory
                        knowledgeGraphAdvisor,     // Brain 0: Knowledge Graph (order: 100)
                        summarizerAdvisor,         // Brain 2: Summarizer (order: 500)
                        refineAdvisor              // Brain 3: Reasoner (order: 1000)
                )
                .defaultTools(aiAgentToolService)
                .build();
    }

    // ============ PHASE 8: INCREMENTAL INDEXING SERVICES ============

    /**
     * Initialize Phase 8 services for incremental indexing
     */
    public void initializePhase8Services(FileHashTracker fileHashTracker,
                                        IncrementalIndexer incrementalIndexer,
                                        IncrementalSummarizer incrementalSummarizer,
                                        IncrementalGraphCalculator incrementalGraphCalculator) {
        logger.info("🚀 Initializing Phase 8: Incremental Indexing Services");
        logger.info("   ✅ FileHashTracker - File change detection");
        logger.info("   ✅ IncrementalIndexer - Incremental chunk indexing");
        logger.info("   ✅ IncrementalSummarizer - Incremental summarization");
        logger.info("   ✅ IncrementalGraphCalculator - Incremental graph updates");
        logger.info("🎯 Phase 8 Services Ready!");
    }

}
