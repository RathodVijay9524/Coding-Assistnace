# 🧠 Current Architecture Overview

## Project Structure

```
src/main/java/com/vijay/
├── config/
│   ├── AIProviderConfig.java          ← Advisor registration & configuration
│   └── AppConfig.java
├── controller/
│   ├── ChatController.java            ← REST API endpoints
│   └── ChatBotController.java         ← Web UI endpoints
├── dto/
│   ├── ChatRequest.java
│   ├── ChatResponse.java
│   └── JudgeAdvisor.java
├── manager/                           ← 🧠 ADVISOR IMPLEMENTATIONS
│   ├── ChainOfThoughtPlannerAdvisor.java    (Brain 0)
│   ├── LocalQueryPlannerAdvisor.java        (Brain 0 variant)
│   ├── IntelligentQueryPlannerAdvisor.java  (Brain 1)
│   ├── ResponseSummarizerAdvisor.java       (Brain 2)
│   ├── MultiCriteriaJudgeAdvisor.java       (Brain 3)
│   ├── LearningSystemAdvisor.java           (Brain 4)
│   ├── UserProfilingAdvisor.java            (Brain 5)
│   ├── ErrorPredictionAdvisor.java          (Brain 6)
│   ├── ConversationMemoryAdvisor.java
│   ├── KnowledgeGraphAdvisor.java
│   ├── EnhancedContextBuilderAdvisor.java
│   ├── EnhancedSelfRefineAdvisor.java
│   ├── SmartQualityAdvisor.java
│   ├── SelfRefineEvaluationAdvisor.java
│   └── QueryPlannerAdvisor.java
├── service/                           ← 🔧 SERVICES
│   ├── ChatService.java               ← Main chat orchestration
│   ├── ConversationMemoryManager.java  ← Memory management
│   ├── UserProfilingService.java       ← User profile tracking
│   ├── LearningMetricsService.java     ← Learning metrics
│   ├── CodeRetrieverService.java       ← Code context retrieval
│   ├── ContextManager.java
│   ├── QueryPlanner.java
│   ├── DependencyGraphBuilder.java
│   ├── CodeChunkIndexer.java
│   ├── CodeSummaryIndexer.java
│   └── MemoryCleanupService.java
├── tools/                             ← 🛠️ TOOL SERVICES
│   ├── AIAgentToolService.java
│   ├── ToolFinderService.java
│   └── ToolIndexingService.java
└── CodingAssistanceApplication.java   ← Main Spring Boot app
```

---

## 🧠 The 7-Brain System (Currently Implemented)

### Brain 0: Chain-of-Thought Planner ✅
**File**: `ChainOfThoughtPlannerAdvisor.java`
**Order**: 0 (Runs FIRST)
**Purpose**: Deep analysis through step-by-step thinking
**Capabilities**:
- Query analysis
- Intent reasoning
- Complexity detection
- Ambiguity assessment

### Brain 1: Intelligent Code Retriever ✅
**File**: `IntelligentQueryPlannerAdvisor.java`
**Order**: 5
**Purpose**: Retrieves relevant code context
**Capabilities**:
- Code search and retrieval
- Context building
- Dependency analysis

### Brain 2: Response Summarizer ✅
**File**: `ResponseSummarizerAdvisor.java`
**Order**: 500
**Purpose**: Summarizes long responses
**Capabilities**:
- Response length detection
- Automatic summarization
- Key point extraction

### Brain 3: Multi-Criteria Judge ✅
**File**: `MultiCriteriaJudgeAdvisor.java`
**Order**: 1000
**Purpose**: Final quality evaluation
**Capabilities**:
- Multi-criteria assessment
- Quality scoring
- Response refinement

### Brain 4: Learning System ✅
**File**: `LearningSystemAdvisor.java`
**Order**: 9
**Purpose**: Pattern learning and optimization
**Capabilities**:
- Query pattern tracking
- Success rate monitoring
- Strategy optimization

### Brain 5: User Profiling ✅
**File**: `UserProfilingAdvisor.java`
**Order**: 2
**Purpose**: User preference adaptation
**Capabilities**:
- Expertise level detection
- Preference learning
- Response adaptation

### Brain 6: Error Prediction ✅
**File**: `ErrorPredictionAdvisor.java`
**Order**: 7
**Purpose**: Proactive error detection
**Capabilities**:
- Ambiguity detection
- Security analysis
- Performance prediction

---

## 🔄 Current Advisor Execution Order

```
Order 0: LocalQueryPlannerAdvisor
Order 1: ConversationMemoryAdvisor
Order 2: UserProfilingAdvisor
Order 3: (AVAILABLE FOR NEW ADVISOR)
Order 4: (AVAILABLE FOR NEW ADVISOR)
Order 5: ChainOfThoughtPlannerAdvisor
Order 6: CodeRetrieverAdvisor
Order 7: ErrorPredictionAdvisor
Order 8: KnowledgeGraphAdvisor
Order 9: LearningSystemAdvisor
Order 500: ResponseSummarizerAdvisor
Order 900: SelfRefineEvaluationAdvisor
Order 1000: MultiCriteriaJudgeAdvisor
```

---

## 💾 Memory System (Currently Implemented)

### ConversationMemoryManager ✅
**File**: `service/ConversationMemoryManager.java`
**Features**:
- Multi-session conversation tracking
- Context-aware memory retrieval
- Automatic memory pruning
- User preference learning
- Query pattern recognition

**Key Classes**:
- `ConversationSession` - Tracks exchanges in a session
- `ConversationExchange` - Single Q&A pair
- `UserProfile` - User preferences and patterns
- `ConversationContext` - Retrieved context for current query

### UserProfilingService ✅
**File**: `service/UserProfilingService.java`
**Features**:
- User expertise level tracking (1-5 scale)
- Preferred response format
- Specialization areas
- Interaction patterns
- Query complexity preferences

### LearningMetricsService ✅
**File**: `service/LearningMetricsService.java`
**Features**:
- Query success rate tracking
- Response quality metrics
- Strategy effectiveness scoring
- Pattern learning

---

## 🔌 How Advisors Work

### Advisor Pattern (Spring AI)
Each advisor implements `CallAdvisor` interface:

```java
@Component
public class MyAdvisor implements CallAdvisor {
    
    @Override
    public String getName() {
        return "MyAdvisor";
    }
    
    @Override
    public int getOrder() {
        return 100; // Execution order
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        // Pre-processing logic
        
        // Call next advisor in chain
        ChatClientResponse response = chain.nextCall(request);
        
        // Post-processing logic
        
        return response;
    }
}
```

### Registration
Advisors are registered in `AIProviderConfig.java`:

```java
@Bean
public ChatClient openAiChatClient(OpenAiChatModel chatModel, 
                                   MyAdvisor myAdvisor) {
    return ChatClient.builder(chatModel)
        .defaultAdvisors(myAdvisor)
        .build();
}
```

---

## 🚀 Multi-Provider Support

### Supported Providers
1. **OpenAI** - GPT-4, GPT-3.5-turbo
2. **Anthropic Claude** - Claude-3, Claude-2
3. **Google Gemini** - Gemini Pro, Gemini Ultra
4. **Ollama** - Local models (Llama, Mistral, CodeLlama)
5. **HuggingFace** - Various open-source models

### Provider Selection
- REST API: `/api/chat/{provider}`
- Web UI: Dropdown selector
- Default: OpenAI

---

## 📊 Data Flow

```
User Query
    ↓
ChatController.chat()
    ↓
ChatService.processChat()
    ↓
Advisor Chain Execution:
    1. LocalQueryPlannerAdvisor (Intent detection)
    2. ConversationMemoryAdvisor (Context retrieval)
    3. UserProfilingAdvisor (User adaptation)
    4. ChainOfThoughtPlannerAdvisor (Deep analysis)
    5. CodeRetrieverAdvisor (Code context)
    6. ErrorPredictionAdvisor (Error detection)
    7. KnowledgeGraphAdvisor (Concept mapping)
    8. LearningSystemAdvisor (Pattern learning)
    9. ResponseSummarizerAdvisor (Summarization)
    10. SelfRefineEvaluationAdvisor (Self-refinement)
    11. MultiCriteriaJudgeAdvisor (Final evaluation)
    ↓
AI Provider (OpenAI/Claude/etc)
    ↓
Response Generation
    ↓
ChatResponse
    ↓
User
```

---

## 🧪 Testing Files

**Location**: `src/test/java/com/vijay/`

Currently minimal - tests should be added for:
- Each advisor independently
- Advisor chain integration
- Memory persistence
- User profiling accuracy
- Learning metrics calculation

---

## 📦 Dependencies (pom.xml)

Key dependencies:
- Spring Boot 3.5.7
- Spring AI (for LLM integration)
- Spring Data JPA (for persistence)
- Thymeleaf (for web UI)
- Lombok (for code generation)
- SLF4J (for logging)

---

## 🔐 Configuration

### Environment Variables Required
```bash
OPENAI_API_KEY=sk-...
ANTHROPIC_API_KEY=sk-ant-...
GOOGLE_API_KEY=...
OPENWEATHERMAP_API_KEY=...
```

### Application Properties
**File**: `src/main/resources/application.properties`

---

## 📝 Important Notes

### DO NOT MODIFY:
- ❌ Advisor execution order without testing
- ❌ ChatService core logic without understanding flow
- ❌ Memory manager without backup
- ❌ Existing advisors without unit tests

### SAFE TO MODIFY:
- ✅ Add new advisors (follow pattern)
- ✅ Add new services (follow pattern)
- ✅ Update configuration
- ✅ Add logging

### BEFORE MAKING CHANGES:
1. Read this document
2. Understand advisor pattern
3. Check existing similar implementation
4. Write unit tests first
5. Test integration before merging

---

## 🎯 Next Phase: Emotional Intelligence

**New Components to Add**:
- EmotionalContextAdvisor (Order: 1)
- TheoryOfMindAdvisor (Order: 3)
- PersonalityAdvisor (Order: 800)

**New Services to Add**:
- EmotionalAnalyzer
- EmotionalToneAdjuster
- MentalStateInferencer
- PersonalityEngine

See `EMOTIONAL_INTELLIGENCE_PLAN.md` for detailed implementation plan.

