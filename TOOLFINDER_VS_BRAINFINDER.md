# ToolFinder vs BrainFinder - Side-by-Side Comparison

## The Pattern: RAG-Based Selection

Both ToolFinder and BrainFinder follow the exact same pattern:

```
1. INDEXING (At Startup)
   ├─ Find all items (Tools / Brains)
   ├─ Extract descriptions
   ├─ Embed descriptions → vectors
   └─ Store in VectorStore

2. RUNTIME (When Query Arrives)
   ├─ Embed user query → vector
   ├─ Semantic search on VectorStore
   ├─ Get Top K most similar items
   └─ Return only relevant items
```

---

## ToolFinder: Finding Relevant Tools

### Problem
```
User: "what is the weather in pune?"

Without ToolFinder:
  LLM receives: ALL 200 tool definitions
  Token cost: ~5000 tokens just for tools
  Result: LLM confused, slow, expensive
```

### Solution: ToolFinder
```
User: "what is the weather in pune?"
                    ↓
Embed query: "what is the weather in pune?" → vector
                    ↓
Search toolVectorStore for Top 3 similar tools
                    ↓
Result: [getCurrentWeather, getWeatherForecast, getWeatherForecast]
                    ↓
Send to LLM: Only these 3 tools + query
                    ↓
Token cost: ~175 tokens (96% savings!)
```

### Code: ToolFinderService
```java
@Service
public class ToolFinderService {
    private final VectorStore vectorStore;  // Contains 200 tool vectors
    
    public List<String> findToolsFor(String prompt) {
        SearchRequest request = SearchRequest.builder()
                .query(prompt)           // "what is the weather in pune?"
                .topK(3)                 // Get top 3 tools
                .build();
        
        List<Document> similarDocuments = vectorStore.similaritySearch(request);
        
        return similarDocuments.stream()
                .map(doc -> (String) doc.getMetadata().get("toolName"))
                .collect(Collectors.toList());
    }
}
```

### Code: ToolIndexingService
```java
@Service
public class ToolIndexingService implements ApplicationRunner {
    private final VectorStore vectorStore;
    private final List<AiToolProvider> allToolProviders;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Document> toolDocuments = new ArrayList<>();
        
        for (AiToolProvider provider : allToolProviders) {
            ToolCallback[] tools = ToolCallbacks.from(provider);
            
            for (ToolCallback tool : tools) {
                String toolName = tool.getToolDefinition().name();
                String description = tool.getToolDefinition().description();
                
                Document toolDoc = new Document(
                        description,
                        Map.of("toolName", toolName)
                );
                toolDocuments.add(toolDoc);
            }
        }
        
        vectorStore.add(toolDocuments);
    }
}
```

---

## BrainFinder: Finding Relevant Brains

### Problem
```
User: "what is 2+4?"

Without BrainFinder (Current):
  ChatClient runs: ALL 13 brains
  - ThoughtStream (unnecessary)
  - QueryPlanner (necessary)
  - EmotionalContext (unnecessary)
  - UserProfiling (unnecessary)
  - CodeRetriever (unnecessary)
  - ... 8 more unnecessary brains
  
  Token cost: ~10,000 tokens
  Response time: 5-10 seconds
  Result: HTTP 413 "Request Too Large" error
```

### Solution: BrainFinder
```
User: "what is 2+4?"
                    ↓
Brain 0 (QueryPlanner) creates plan
                    ↓
Embed plan: "simple math calculation" → vector
                    ↓
Search brainVectorStore for Top 3-4 similar brains
                    ↓
Result: [QueryPlanner, Judge, ResponseSummarizer]
                    ↓
Build dynamic ChatClient with ONLY these 3 brains
                    ↓
Run query through selected brains
                    ↓
Token cost: ~2,000 tokens (80% savings!)
Response time: 1-2 seconds
```

### Code: BrainFinderService
```java
@Service
public class BrainFinderService {
    private final VectorStore brainVectorStore;  // Contains 13 brain vectors
    
    public List<String> findBrainsFor(String query) {
        SearchRequest request = SearchRequest.builder()
                .query(query)            // "simple math calculation"
                .topK(4)                 // Get top 4 brains
                .build();
        
        List<Document> similarDocuments = brainVectorStore.similaritySearch(request);
        
        return similarDocuments.stream()
                .map(doc -> (String) doc.getMetadata().get("brainName"))
                .collect(Collectors.toList());
    }
}
```

### Code: BrainIndexerService
```java
@Service
public class BrainIndexerService implements ApplicationRunner {
    private final VectorStore brainVectorStore;
    private final List<IAgentBrain> allBrains;  // All 13 brains
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Document> brainDocuments = new ArrayList<>();
        
        for (IAgentBrain brain : allBrains) {
            String brainName = brain.getBrainName();
            String description = brain.getBrainDescription();
            
            Document brainDoc = new Document(
                    description,
                    Map.of("brainName", brainName)
            );
            brainDocuments.add(brainDoc);
        }
        
        brainVectorStore.add(brainDocuments);
    }
}
```

---

## Side-by-Side Comparison

| Aspect | ToolFinder | BrainFinder |
|--------|-----------|------------|
| **Purpose** | Find relevant tools | Find relevant brains |
| **Items** | 200+ tools | 13 brains |
| **VectorStore** | toolVectorStore | brainVectorStore |
| **Indexer** | ToolIndexingService | BrainIndexerService |
| **Finder** | ToolFinderService | BrainFinderService |
| **Interface** | AiToolProvider | IAgentBrain |
| **Search Query** | User query | Query/Plan |
| **Top K** | 3 | 4 |
| **Token Savings** | 96% | 80% |
| **Use Case** | Avoid sending all tools | Avoid running all brains |

---

## How They Work Together

### Current Architecture (Before Brain RAG)
```
User Query
    ↓
ChatService
    ↓
ChatClient with ALL 13 brains
    ├─ Brain -1: ThoughtStream
    ├─ Brain 0: QueryPlanner
    ├─ Brain 1-13: All others
    ↓
Response
```

### New Architecture (With Brain RAG)
```
User Query
    ↓
ChatService (Dynamic Conductor)
    ├─ Brain 0: QueryPlanner (always runs)
    ├─ BrainFinderService (semantic search)
    │   └─ Find Top 3-4 relevant brains
    ├─ Build dynamic ChatClient with selected brains
    │   ├─ Brain 0: QueryPlanner
    │   ├─ Brain 8: ResponseSummarizer
    │   └─ Brain 13: Judge
    ↓
Response
```

---

## Example Scenarios

### Scenario 1: Math Query
```
User: "what is 2+4?"

BrainFinder selects: [QueryPlanner, Judge, ResponseSummarizer]
Skips: EmotionalContext, UserProfiling, CodeRetriever, etc.

Result: Fast, accurate, efficient
```

### Scenario 2: Code Query
```
User: "explain the add() method in AIAgentToolService"

BrainFinder selects: [QueryPlanner, CodeRetriever, ResponseSummarizer, Judge]
Skips: EmotionalContext, UserProfiling, etc.

Result: Code-focused, accurate
```

### Scenario 3: Emotional Query
```
User: "I'm feeling sad about my code not working"

BrainFinder selects: [QueryPlanner, EmotionalContext, UserProfiling, ResponseSummarizer]
Skips: CodeRetriever, etc.

Result: Empathetic, personalized
```

---

## The Genius of This Pattern

Both ToolFinder and BrainFinder solve the same fundamental problem:

**Problem:** Too many options (tools/brains) → LLM confusion → slow, expensive, errors

**Solution:** Semantic search → Find only relevant options → LLM focus → fast, cheap, accurate

**Pattern:** Embed → Search → Select → Use

This is the RAG (Retrieval-Augmented Generation) pattern applied to:
1. Tools (ToolFinder)
2. Brains (BrainFinder)
3. Code (CodeChunkIndexer, CodeSummaryIndexer)

All using the same underlying principle: **Semantic similarity search**

---

## Implementation Roadmap

### Phase 1: Foundation (DONE ✅)
- ✅ IAgentBrain interface created
- ✅ BrainFinderService created
- ✅ BrainIndexerService created
- ✅ brainVectorStore bean added

### Phase 2: Integration (NEXT)
- ⏳ Update all 13 advisors to implement IAgentBrain
- ⏳ Add getBrainName(), getBrainDescription(), getOrder() to each advisor

### Phase 3: Dynamic Conductor (NEXT)
- ⏳ Rewrite ChatService to use BrainFinderService
- ⏳ Build dynamic ChatClient based on selected brains
- ⏳ Test with various query types

### Phase 4: Optimization (FUTURE)
- ⏳ Fine-tune topK (currently 4, maybe 3-5)
- ⏳ Add brain selection logging/monitoring
- ⏳ Cache brain selections for similar queries
