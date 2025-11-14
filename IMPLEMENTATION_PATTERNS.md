# 📋 Implementation Patterns & Code Templates

## Pattern 1: Creating a New Service

**Location**: `src/main/java/com/vijay/service/`

```java
package com.vijay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 🧠 Service Description
 * 
 * Responsibilities:
 * - What it does
 * - How it helps
 */
@Service
public class MyNewService {
    
    private static final Logger logger = LoggerFactory.getLogger(MyNewService.class);
    
    // Constructor injection
    public MyNewService() {
        logger.info("✅ MyNewService initialized");
    }
    
    // Public methods
    public String doSomething(String input) {
        logger.debug("Processing: {}", input);
        
        // Implementation
        String result = "processed";
        
        logger.debug("Result: {}", result);
        return result;
    }
}
```

**Key Points**:
- ✅ Use `@Service` annotation
- ✅ Use SLF4J logger
- ✅ Add comprehensive logging
- ✅ Use constructor injection (not @Autowired)
- ✅ Add JavaDoc with emoji
- ✅ Keep methods focused and testable

---

## Pattern 2: Creating a New Advisor

**Location**: `src/main/java/com/vijay/manager/`

```java
package com.vijay.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

/**
 * 🧠 Brain X: My Advisor Name
 * 
 * Purpose: What this brain does
 * 
 * Responsibilities:
 * - Responsibility 1
 * - Responsibility 2
 * 
 * Execution Order: X (when it runs)
 */
@Component
public class MyAdvisor implements CallAdvisor {
    
    private static final Logger logger = LoggerFactory.getLogger(MyAdvisor.class);
    
    private final MyNewService myService;
    
    public MyAdvisor(MyNewService myService) {
        this.myService = myService;
    }
    
    @Override
    public String getName() {
        return "MyAdvisor";
    }
    
    @Override
    public int getOrder() {
        return 10; // Execution order
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("🧠 Brain X (My Advisor): Starting analysis...");
        
        try {
            // Extract user message
            String userQuery = extractUserMessage(request);
            if (userQuery.isEmpty()) {
                logger.info("⚠️ Brain X: Empty query, proceeding");
                return chain.nextCall(request);
            }
            
            // Pre-processing logic
            String analysis = myService.doSomething(userQuery);
            logger.info("🔍 Brain X: Analysis result - {}", analysis);
            
            // Continue to next advisor
            ChatClientResponse response = chain.nextCall(request);
            
            // Post-processing logic (optional)
            logger.info("✅ Brain X: Complete");
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Brain X: Error - {}", e.getMessage(), e);
            return chain.nextCall(request);
        }
    }
    
    private String extractUserMessage(ChatClientRequest request) {
        try {
            for (var message : request.messages()) {
                if (message instanceof UserMessage) {
                    return ((UserMessage) message).getContent();
                }
            }
        } catch (Exception e) {
            logger.debug("Could not extract user message: {}", e.getMessage());
        }
        return "";
    }
}
```

**Key Points**:
- ✅ Implement `CallAdvisor` interface
- ✅ Use `@Component` annotation
- ✅ Define `getName()` and `getOrder()`
- ✅ Implement `adviseCall()` method
- ✅ Always call `chain.nextCall(request)` to continue chain
- ✅ Handle exceptions gracefully
- ✅ Add comprehensive logging with emojis
- ✅ Use constructor injection for dependencies

---

## Pattern 3: Creating a Model/DTO

**Location**: `src/main/java/com/vijay/dto/` or `src/main/java/com/vijay/service/`

```java
package com.vijay.dto;

import java.time.LocalDateTime;

/**
 * Model representing X
 */
public class MyModel {
    
    private String id;
    private String name;
    private int score;
    private LocalDateTime createdAt;
    
    // Constructor
    public MyModel(String id, String name, int score) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    // toString for logging
    @Override
    public String toString() {
        return "MyModel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", score=" + score +
                ", createdAt=" + createdAt +
                '}';
    }
}
```

**Key Points**:
- ✅ Use clear, descriptive names
- ✅ Include JavaDoc
- ✅ Provide constructors
- ✅ Provide getters/setters
- ✅ Override `toString()` for logging
- ✅ Use immutable fields where possible

---

## Pattern 4: Unit Testing

**Location**: `src/test/java/com/vijay/service/`

```java
package com.vijay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MyNewService Tests")
class MyNewServiceTest {
    
    private MyNewService service;
    
    @BeforeEach
    void setUp() {
        service = new MyNewService();
    }
    
    @Test
    @DisplayName("Should process input correctly")
    void testDoSomething() {
        // Arrange
        String input = "test";
        
        // Act
        String result = service.doSomething(input);
        
        // Assert
        assertNotNull(result);
        assertEquals("processed", result);
    }
    
    @Test
    @DisplayName("Should handle empty input")
    void testDoSomethingWithEmpty() {
        // Arrange
        String input = "";
        
        // Act
        String result = service.doSomething(input);
        
        // Assert
        assertNotNull(result);
    }
}
```

**Key Points**:
- ✅ Use JUnit 5
- ✅ Use `@DisplayName` for clarity
- ✅ Follow AAA pattern (Arrange, Act, Assert)
- ✅ Test both happy path and edge cases
- ✅ Use descriptive test names
- ✅ One assertion per test (ideally)

---

## Pattern 5: Registering New Advisor

**File**: `src/main/java/com/vijay/config/AIProviderConfig.java`

```java
// Add to existing ChatClient beans:

@Bean
public ChatClient openAiChatClient(
    OpenAiChatModel chatModel,
    MyAdvisor myAdvisor,
    // ... other advisors
) {
    return ChatClient.builder(chatModel)
        .defaultAdvisors(
            myAdvisor,
            // ... other advisors in order
        )
        .build();
}

// Do the same for other providers:
// - anthropicChatClient
// - googleChatClient
// - ollamaChatClient
// - huggingFaceChatClient
```

**Key Points**:
- ✅ Add advisor to all ChatClient beans
- ✅ Maintain order (lower order numbers first)
- ✅ Use constructor injection
- ✅ Test that all providers still work

---

## Pattern 6: Logging Best Practices

```java
// ✅ DO: Use appropriate log levels
logger.debug("🔍 Detailed debug info: {}", variable);
logger.info("ℹ️ Important info: {}", variable);
logger.warn("⚠️ Warning: {}", variable);
logger.error("❌ Error occurred: {}", variable, exception);

// ✅ DO: Use emojis for visual clarity
logger.info("🧠 Brain X: Processing");
logger.info("✅ Success");
logger.info("❌ Failed");
logger.info("🔍 Analyzing");
logger.info("💾 Storing");

// ❌ DON'T: Use string concatenation
logger.info("Result: " + result); // BAD

// ✅ DO: Use parameterized logging
logger.info("Result: {}", result); // GOOD

// ❌ DON'T: Log sensitive information
logger.info("API Key: {}", apiKey); // BAD

// ✅ DO: Log meaningful information
logger.info("Processing query from user: {}", userId); // GOOD
```

---

## Pattern 7: Error Handling

```java
// ✅ DO: Handle exceptions gracefully
try {
    String result = riskyOperation();
    logger.info("✅ Operation successful");
    return result;
} catch (SpecificException e) {
    logger.error("❌ Specific error: {}", e.getMessage());
    // Handle gracefully
    return fallbackValue;
} catch (Exception e) {
    logger.error("❌ Unexpected error: {}", e.getMessage(), e);
    // Handle gracefully
    return fallbackValue;
}

// ✅ DO: Always provide fallback in advisors
@Override
public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
    try {
        // Processing
        return chain.nextCall(request);
    } catch (Exception e) {
        logger.error("❌ Error in advisor: {}", e.getMessage(), e);
        // Always continue the chain
        return chain.nextCall(request);
    }
}
```

---

## Pattern 8: Integration with Memory System

```java
// In your service, inject ConversationMemoryManager
@Service
public class MyService {
    
    private final ConversationMemoryManager memoryManager;
    
    public MyService(ConversationMemoryManager memoryManager) {
        this.memoryManager = memoryManager;
    }
    
    public void storeResult(String sessionId, String userId, String query, String response) {
        // Store in memory
        memoryManager.storeConversation(
            sessionId,
            userId,
            query,
            response,
            "my_strategy",
            0.95 // confidence
        );
        
        logger.info("💾 Result stored in memory");
    }
    
    public ConversationMemoryManager.ConversationContext getContext(
        String sessionId, 
        String userId, 
        String query) {
        
        return memoryManager.getRelevantContext(sessionId, userId, query);
    }
}
```

---

## Pattern 9: Integration with User Profiling

```java
// In your service, inject UserProfilingService
@Service
public class MyService {
    
    private final UserProfilingService userProfilingService;
    
    public MyService(UserProfilingService userProfilingService) {
        this.userProfilingService = userProfilingService;
    }
    
    public void recordUserInteraction(String userId, String queryType, int quality) {
        userProfilingService.recordInteraction(
            userId,
            queryType,
            quality,
            "specialization_area"
        );
        
        logger.info("👤 User interaction recorded");
    }
    
    public int getUserExpertiseLevel(String userId) {
        return userProfilingService.detectExpertiseLevel(userId);
    }
}
```

---

## Pattern 10: Extracting User Message from Request

```java
// Common utility method in advisors
private String extractUserMessage(ChatClientRequest request) {
    try {
        for (var message : request.messages()) {
            if (message instanceof UserMessage) {
                return ((UserMessage) message).getContent();
            }
        }
    } catch (Exception e) {
        logger.debug("Could not extract user message: {}", e.getMessage());
    }
    return "";
}

// Usage
String userQuery = extractUserMessage(request);
if (!userQuery.isEmpty()) {
    // Process query
}
```

---

## Quick Checklist for New Components

### New Service
- [ ] Add `@Service` annotation
- [ ] Add SLF4J logger
- [ ] Use constructor injection
- [ ] Add JavaDoc with emoji
- [ ] Add comprehensive logging
- [ ] Write unit tests
- [ ] Handle exceptions gracefully

### New Advisor
- [ ] Add `@Component` annotation
- [ ] Implement `CallAdvisor` interface
- [ ] Define `getName()` and `getOrder()`
- [ ] Implement `adviseCall()` method
- [ ] Always call `chain.nextCall(request)`
- [ ] Add comprehensive logging
- [ ] Register in `AIProviderConfig.java`
- [ ] Write unit tests
- [ ] Test with all providers

### New Model/DTO
- [ ] Add JavaDoc
- [ ] Provide constructors
- [ ] Provide getters/setters
- [ ] Override `toString()`
- [ ] Consider immutability

### Integration
- [ ] Test with existing code
- [ ] Check performance impact
- [ ] Verify logging output
- [ ] Test with all providers
- [ ] Update documentation

---

## File Naming Conventions

- **Services**: `MyFeatureService.java`
- **Advisors**: `MyFeatureAdvisor.java`
- **Models/DTOs**: `MyModel.java` or `MyModelDto.java`
- **Tests**: `MyFeatureServiceTest.java`
- **Enums**: `MyEnum.java`
- **Interfaces**: `IMyInterface.java` (optional, not commonly used)

---

## Emoji Usage Guide

| Emoji | Usage |
|-------|-------|
| 🧠 | Brain/Advisor/Cognitive |
| 👤 | User/Profile |
| 💾 | Storage/Memory |
| 🔍 | Search/Analysis |
| ✅ | Success/Complete |
| ❌ | Error/Failed |
| ⚠️ | Warning |
| 🔄 | Processing/Loop |
| 📋 | Data/Context |
| 🎯 | Intent/Goal |
| 💭 | Thinking/Analysis |
| 🤔 | Reasoning |
| 📊 | Metrics/Stats |
| 🚀 | Launch/Start |
| 🛠️ | Tools |
| 📈 | Growth/Improvement |
| ℹ️ | Information |

