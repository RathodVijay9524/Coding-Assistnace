# 🎨 Visual Architecture Overview

## Current System Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                        USER QUERY                               │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    ChatController                               │
│                  (REST API Handler)                             │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    ChatService                                  │
│              (Query Orchestration)                              │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
        ┌────────────────────────────────────────┐
        │    ADVISOR CHAIN EXECUTION             │
        │    (In Order of Execution)             │
        └────────────────────────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
    ┌────────┐          ┌────────┐          ┌────────┐
    │ Brain  │          │ Brain  │          │ Brain  │
    │   0    │          │   1    │          │   2    │
    │ CoT    │          │ Code   │          │ Summary│
    │Planner │          │Retriev │          │        │
    └────────┘          └────────┘          └────────┘
        │                    │                    │
        └────────────────────┼────────────────────┘
                             │
                             ▼
        ┌────────────────────────────────────────┐
        │    AI PROVIDER                         │
        │  (OpenAI/Claude/Google/Ollama/HF)     │
        └────────────────────────────────────────┘
                             │
                             ▼
        ┌────────────────────────────────────────┐
        │    RESPONSE GENERATION                 │
        └────────────────────────────────────────┘
```

---

## 7-Brain System Architecture

```
                    🧠 MULTI-BRAIN SYSTEM 🧠
                    
Brain 0: Chain-of-Thought Planner
├─ Query Analysis
├─ Intent Reasoning
├─ Complexity Detection
└─ Ambiguity Assessment

Brain 1: Intelligent Code Retriever
├─ Code Search
├─ Context Building
├─ Dependency Analysis
└─ Code Ranking

Brain 2: Response Summarizer
├─ Length Detection
├─ Key Point Extraction
├─ Automatic Summarization
└─ Conciseness Optimization

Brain 3: Multi-Criteria Judge
├─ Quality Assessment
├─ Relevance Scoring
├─ Accuracy Evaluation
└─ Response Refinement

Brain 4: Learning System
├─ Pattern Recognition
├─ Success Rate Tracking
├─ Strategy Optimization
└─ Metric Collection

Brain 5: User Profiling
├─ Expertise Detection
├─ Preference Learning
├─ Specialization Tracking
└─ Response Adaptation

Brain 6: Error Prediction
├─ Ambiguity Detection
├─ Security Analysis
├─ Performance Prediction
└─ Deprecation Checking
```

---

## Current Advisor Execution Order

```
Order 0:  LocalQueryPlannerAdvisor
Order 1:  ConversationMemoryAdvisor
Order 2:  UserProfilingAdvisor
Order 3:  (AVAILABLE)
Order 4:  (AVAILABLE)
Order 5:  ChainOfThoughtPlannerAdvisor
Order 6:  CodeRetrieverAdvisor
Order 7:  ErrorPredictionAdvisor
Order 8:  KnowledgeGraphAdvisor
Order 9:  LearningSystemAdvisor
Order 500: ResponseSummarizerAdvisor
Order 900: SelfRefineEvaluationAdvisor
Order 1000: MultiCriteriaJudgeAdvisor
```

---

## Phase 3: Emotional Intelligence Integration

```
NEW ADVISOR EXECUTION ORDER (Phase 3)

Order 0:  LocalQueryPlannerAdvisor
Order 1:  ⭐ EmotionalContextAdvisor (NEW)
Order 2:  ConversationMemoryAdvisor
Order 3:  ⭐ TheoryOfMindAdvisor (NEW)
Order 4:  UserProfilingAdvisor
Order 5:  ChainOfThoughtPlannerAdvisor
Order 6:  CodeRetrieverAdvisor
Order 7:  ErrorPredictionAdvisor
Order 8:  KnowledgeGraphAdvisor
Order 9:  LearningSystemAdvisor
Order 500: ResponseSummarizerAdvisor
Order 800: ⭐ PersonalityAdvisor (NEW)
Order 900: SelfRefineEvaluationAdvisor
Order 1000: MultiCriteriaJudgeAdvisor
```

---

## Emotional Intelligence Components

```
EMOTIONAL CONTEXT ENGINE (Week 1)
├─ EmotionalState (Enum)
├─ EmotionalAnalyzer (Service)
├─ EmotionalToneAdjuster (Service)
└─ EmotionalContextAdvisor (Advisor)

THEORY OF MIND (Week 2)
├─ UserMentalModel (Model)
├─ MentalStateInferencer (Service)
└─ TheoryOfMindAdvisor (Advisor)

PERSONALITY ENGINE (Week 3)
├─ PersonalityTraits (Model)
├─ CommunicationStyle (Model)
├─ PersonalityEngine (Service)
└─ PersonalityAdvisor (Advisor)
```

---

## Completion Progress

```
CURRENT STATUS: 57% COMPLETE

Architecture Foundation:        95% ✅
Cognitive Layers (7 Brains):   100% ✅
Human Thinking:                 40% ❌ (We're fixing this!)
Advanced Capabilities:          20% ❌
Learning & Growth:             60% ⚠️

AFTER PHASE 3: Expected 70-75%
```

---

## Memory System

```
CONVERSATION MEMORY MANAGER

Active Sessions (In-Memory)
├─ Session 1: [Exchange 1, Exchange 2, ...]
├─ Session 2: [...]
└─ Session N: [...]

User Profiles (In-Memory)
├─ User 1: {expertise, preferences, specializations}
├─ User 2: {...}
└─ User N: {...}

Long-Term Memory (Persistent)
├─ Important Exchanges
├─ Key Learnings
├─ User Patterns
└─ Historical Data
```

---

## Key Integration Points

```
ChatService
└─ No changes needed (advisors handle everything)

AIProviderConfig
├─ Register EmotionalContextAdvisor (Order: 1)
├─ Register TheoryOfMindAdvisor (Order: 3)
└─ Register PersonalityAdvisor (Order: 800)

ConversationMemoryManager
├─ Store emotional context
└─ Track emotional patterns

UserProfilingService
├─ Extend with emotional preferences
└─ Track personality compatibility
```

