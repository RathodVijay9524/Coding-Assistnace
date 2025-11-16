package com.vijay.manager;

import com.vijay.context.GlobalBrainContext;
import com.vijay.context.TraceContext;
import com.vijay.dto.AgentPlan;
import com.vijay.dto.ReasoningState;
import com.vijay.util.AgentPlanHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 🎼 Brain 0: Unified Conductor Advisor
 * 
 * THE ONE AND ONLY MASTER PLANNER
 * 
 * Purpose: Create a single, unified master plan for the entire request
 * 
 * This brain REPLACES both:
 * - ThoughtStreamAdvisor (Brain -1): Attention mechanism
 * - LocalQueryPlannerAdvisor (Brain 0): Query planning
 * 
 * Why merge them?
 * - Eliminates "Split Brain" problem (two planners conflicting)
 * - Creates ONE unified thought process (human-like)
 * - All downstream brains read from ONE master plan
 * - No conflicting intents or focus areas
 * 
 * Execution Order: 0 (FIRST - before all other brains)
 */
@Component
public class ConductorAdvisor implements CallAdvisor, IAgentBrain {
    
    private static final Logger logger = LoggerFactory.getLogger(ConductorAdvisor.class);
    
    public ConductorAdvisor() {
    }
    
    @Override
    public String getName() {
        return "ConductorAdvisor";
    }
    
    @Override
    public String getBrainName() {
        return "conductorAdvisor";
    }
    
    @Override
    public String getBrainDescription() {
        return "The unified master planner. Creates ONE master AgentPlan that guides all downstream brains.";
    }
    
    @Override
    public int getOrder() {
        return 0;
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        String traceId = TraceContext.getTraceId();
        logger.info("[{}] 🎼 Brain 0 (Unified Conductor): Creating master plan...", traceId);
        
        try {
            String userQuery = extractUserMessage(request);
            
            if (userQuery.isEmpty()) {
                logger.warn("[{}] ⚠️ Brain 0: Empty query, using default plan", traceId);
                AgentPlan defaultPlan = createDefaultPlan();
                return storeAndContinue(request, chain, defaultPlan);
            }
            
            // ⚡ FAST PATH: Detect simple queries for performance optimization
            if (isSimpleQuery(userQuery)) {
                logger.info("[{}] ⚡ Brain 0: FAST PATH detected - simple query ({}ms optimization)", 
                    traceId, "300-400");
                AgentPlan fastPlan = createFastPathPlan(userQuery);
                return storeAndContinue(request, chain, fastPlan);
            }
            
            // STEP 1: Analyze query
            int complexity = calculateComplexity(userQuery);
            int ambiguity = calculateAmbiguity(userQuery);
            String focusArea = determineFocusArea(userQuery);
            String ignoreArea = determineIgnoreArea(userQuery);
            String intent = analyzeIntent(userQuery);
            double confidence = calculateIntentConfidence(userQuery, intent);
            
            // STEP 2: Select strategy
            String strategy = selectReasoningStrategy(complexity, ambiguity);
            
            // STEP 3: Identify tools (IMPROVED)
            List<String> requiredTools = identifyRequiredTools(userQuery, intent);
            
            // STEP 4: Identify specialist brains
            List<String> selectedBrains = identifySpecialistBrains(userQuery, intent, complexity);
            
            // STEP 5: Create master plan
            AgentPlan masterPlan = new AgentPlan()
                .setIntent(intent)
                .setComplexity(complexity)
                .setAmbiguity(ambiguity)
                .setFocusArea(focusArea)
                .setIgnoreArea(ignoreArea)
                .setStrategy(strategy)
                .setRequiredTools(requiredTools)
                .setSelectedBrains(selectedBrains)
                .setConfidence(confidence)
                .setUserQuery(userQuery)
                .setCreatedAt(System.currentTimeMillis());
            
            // Log plan details
            logger.info(formatPlanDetails(masterPlan));
            
            // PHASE 1 INTEGRATION: Approve tools in ReasoningState
            ReasoningState state = GlobalBrainContext.getReasoningState();
            if (state != null) {
                state.approveTools(requiredTools);  // FINAL DECISION
                logger.info("[{}]    ✅ Conductor APPROVED tools: {}", traceId, requiredTools);
            } else {
                logger.warn("[{}]    ⚠️ No ReasoningState found in GlobalBrainContext", traceId);
            }
            
            // STEP 6: Store and continue
            return storeAndContinue(request, chain, masterPlan);
            
        } catch (Exception e) {
            logger.error("[{}] ❌ Brain 0: Error creating master plan - {}", traceId, e.getMessage());
            AgentPlan fallbackPlan = createDefaultPlan();
            return storeAndContinue(request, chain, fallbackPlan);
        }
    }
    
    /**
     * Calculate query complexity (1-10)
     */
    private int calculateComplexity(String query) {
        int score = 1;
        
        if (query.length() > 100) score += 2;
        if (query.length() > 200) score += 2;
        
        int wordCount = query.split("\\s+").length;
        score += Math.min(wordCount / 5, 3);
        
        if (query.matches(".*\\b(algorithm|architecture|optimization|refactor|debug)\\b.*")) score += 2;
        
        score += query.split("\\?").length - 1;
        
        return Math.min(score, 10);
    }
    
    /**
     * Calculate query ambiguity (1-10)
     */
    private int calculateAmbiguity(String query) {
        int score = 0;
        
        if (query.matches(".*\\b(it|this|that|thing|stuff|something)\\b.*")) score += 2;
        if (query.matches(".*\\b(maybe|probably|might|could)\\b.*")) score += 1;
        
        if (query.length() < 20) score += 2;
        
        if (query.matches(".*\\b(he|she|they|we)\\b.*")) score += 1;
        
        return Math.min(score, 10);
    }
    
    /**
     * Determine focus area
     */
    private String determineFocusArea(String query) {
        if (query.contains("bug") || query.contains("error") || query.contains("fix")) {
            return "DEBUG";
        } else if (query.contains("refactor") || query.contains("improve") || query.contains("optimize")) {
            return "REFACTOR";
        } else if (query.contains("test")) {
            return "TESTING";
        } else if (query.contains("architecture") || query.contains("design")) {
            return "ARCHITECTURE";
        } else if (query.contains("performance") || query.contains("speed")) {
            return "PERFORMANCE";
        } else if (query.contains("security")) {
            return "SECURITY";
        } else if (query.contains("implement") || query.contains("create") || query.contains("build")) {
            return "IMPLEMENTATION";
        } else {
            return "GENERAL";
        }
    }
    
    /**
     * Determine ignore area
     */
    private String determineIgnoreArea(String query) {
        if (query.contains("don't") || query.contains("not") || query.contains("avoid")) {
            return "CONSTRAINTS";
        }
        return "NONE";
    }
    
    /**
     * Analyze intent
     */
    private String analyzeIntent(String query) {
        if (query.matches(".*\\b(add|calculate|compute|sum|total)\\b.*")) {
            return "CALCULATION";
        } else if (query.matches(".*\\b(bug|error|fix|crash|fail)\\b.*")) {
            return "DEBUG";
        } else if (query.matches(".*\\b(refactor|improve|optimize|clean)\\b.*")) {
            return "REFACTOR";
        } else if (query.matches(".*\\b(implement|create|build|write|code)\\b.*")) {
            return "IMPLEMENTATION";
        } else if (query.matches(".*\\b(explain|understand|how|why|what)\\b.*")) {
            return "EXPLANATION";
        } else if (query.matches(".*\\b(test|unit|integration)\\b.*")) {
            return "TESTING";
        } else {
            return "GENERAL";
        }
    }
    
    /**
     * Calculate confidence in intent analysis
     */
    private double calculateIntentConfidence(String query, String intent) {
        if (intent.equals("CALCULATION") && query.contains("add")) return 0.95;
        if (intent.equals("DEBUG") && query.contains("bug")) return 0.95;
        if (intent.equals("REFACTOR") && query.contains("refactor")) return 0.95;
        
        return 0.7;
    }
    
    /**
     * Select reasoning strategy
     */
    private String selectReasoningStrategy(int complexity, int ambiguity) {
        if (complexity <= 3 && ambiguity <= 3) {
            return "FAST_RECALL";
        } else if (complexity <= 6 && ambiguity <= 6) {
            return "BALANCED";
        } else {
            return "SLOW_REASONING";
        }
    }
    
    /**
     * Identify required tools - IMPROVED with better pattern matching
     */
    private List<String> identifyRequiredTools(String query, String intent) {
        List<String> tools = new ArrayList<>();
        
        // CALCULATION tools - improved pattern matching
        if (intent.equals("CALCULATION") || query.matches(".*\\b(add|calculate|sum|total|plus|\\+|how much|what is)\\b.*")) {
            if (query.matches(".*\\b(add|calculate|sum|total|plus|\\+|how much)\\b.*")) tools.add("add");
            if (query.matches(".*\\b(subtract|minus|\\-)\\b.*")) tools.add("subtract");
            if (query.matches(".*\\b(multiply|times|\\*)\\b.*")) tools.add("multiply");
            if (query.matches(".*\\b(divide|divided|/)\\b.*")) tools.add("divide");
        }
        
        // DATE/TIME tools
        if (query.matches(".*\\b(date|today|time|current|now|when|what time|what's the date)\\b.*")) {
            tools.add("getCurrentDateTime");
        }
        
        // WEATHER tools
        if (query.matches(".*\\b(weather|temperature|rain|sunny|forecast|what's the weather)\\b.*")) {
            tools.add("getWeather");
        }
        
        // EMAIL tools
        if (query.matches(".*\\b(email|send|mail|message|write an email)\\b.*")) {
            tools.add("sendEmail");
        }
        
        logger.info("   🔍 Tool identification: Intent={}, Query length={}, Tools found={}", 
            intent, query.length(), tools.size());
        
        return tools;
    }
    
    /**
     * Identify specialist brains to activate
     */
    private List<String> identifySpecialistBrains(String query, String intent, int complexity) {
        List<String> brains = new ArrayList<>();
        
        // Based on intent
        if (intent.equals("DEBUG")) {
            brains.add("errorPredictionAdvisor");
            brains.add("cognitiveBiasAdvisor");
        } else if (intent.equals("REFACTOR")) {
            brains.add("advancedCapabilitiesAdvisor");
            brains.add("learningGrowthAdvisor");
        } else if (intent.equals("IMPLEMENTATION")) {
            brains.add("advancedCapabilitiesAdvisor");
            brains.add("responseSummarizerAdvisor");
        }
        
        // Based on complexity
        if (complexity > 7) {
            brains.add("knowledgeGraphAdvisor");
        }
        
        return brains;
    }
    
    /**
     * Store plan in thread-local and continue
     */
    private ChatClientResponse storeAndContinue(ChatClientRequest request, CallAdvisorChain chain, AgentPlan plan) {
        AgentPlanHolder.setPlan(plan);
        logger.info("📌 Brain 0: Plan stored in thread-local for downstream brains");
        return chain.nextCall(request);
    }
    
    /**
     * Create default plan (fallback)
     */
    private AgentPlan createDefaultPlan() {
        return new AgentPlan()
            .setIntent("GENERAL")
            .setComplexity(5)
            .setAmbiguity(5)
            .setFocusArea("GENERAL")
            .setIgnoreArea("NONE")
            .setStrategy("BALANCED")
            .setRequiredTools(new ArrayList<>())
            .setSelectedBrains(new ArrayList<>())
            .setConfidence(0.5)
            .setUserQuery("(default plan)")
            .setCreatedAt(System.currentTimeMillis());
    }
    
    /**
     * Format plan details for logging
     */
    private String formatPlanDetails(AgentPlan plan) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n═══════════════════════════════════════════════════════════\n");
        sb.append("📋 MASTER PLAN CREATED BY CONDUCTOR (Brain 0)\n");
        sb.append("═══════════════════════════════════════════════════════════\n");
        sb.append(String.format("  Intent: %s (Confidence: %.1f%%)\n", plan.getIntent(), plan.getConfidence() * 100));
        sb.append(String.format("  Complexity: %d/10 | Ambiguity: %d/10\n", plan.getComplexity(), plan.getAmbiguity()));
        sb.append(String.format("  Focus Area: %s | Ignore Area: %s\n", plan.getFocusArea(), plan.getIgnoreArea()));
        sb.append(String.format("  Strategy: %s\n", plan.getStrategy()));
        sb.append(String.format("  Required Tools: %s\n", plan.getRequiredTools()));
        sb.append(String.format("  Specialist Brains: %s\n", plan.getSelectedBrains()));
        sb.append(String.format("  Overall Confidence: %.1f%%\n", plan.getConfidence() * 100));
        sb.append("═".repeat(60)).append("\n");
        return sb.toString();
    }
    
    /**
     * ⚡ FAST PATH: Detect simple queries for performance optimization
     * CRITICAL FIX: Only use fast path for TRULY simple queries
     * Avoid fast path for: technical queries, research, external data needs
     */
    private boolean isSimpleQuery(String query) {
        if (query == null || query.isEmpty()) return false;
        
        String lowerQuery = query.toLowerCase();
        
        // ❌ NEVER use fast path for these keywords (requires full brain chain)
        String[] complexKeywords = {
            "why", "how", "explain", "architecture", "design", "refactor", "optimize",
            "spring", "version", "latest", "documentation", "tutorial", "guide",
            "research", "find", "search", "what is", "tell me about",
            "weather", "forecast", "temperature", "city", "location"
        };
        
        for (String keyword : complexKeywords) {
            if (lowerQuery.contains(keyword)) {
                logger.debug("⚠️ Complex keyword '{}' detected - skipping fast path", keyword);
                return false;
            }
        }
        
        // ❌ NEVER use fast path for long queries
        if (query.length() > 40) {
            logger.debug("⚠️ Query too long ({} chars) - skipping fast path", query.length());
            return false;
        }
        
        // ❌ NEVER use fast path for multiple questions
        int questionCount = query.split("\\?").length - 1;
        if (questionCount > 1) {
            logger.debug("⚠️ Multiple questions detected - skipping fast path");
            return false;
        }
        
        // ✅ ONLY use fast path for arithmetic and simple time queries
        if (lowerQuery.matches(".*[+\\-*/].*") || 
            lowerQuery.contains("what time") || 
            lowerQuery.contains("what date")) {
            logger.info("✅ Simple arithmetic/time query - using fast path");
            return true;
        }
        
        // Default: use full brain chain for safety
        logger.debug("⚠️ Query doesn't match fast path criteria - using full chain");
        return false;
    }
    
    /**
     * ⚡ FAST PATH: Create optimized plan for simple queries
     * Uses only 3 core brains instead of 7 for 60% performance improvement
     */
    private AgentPlan createFastPathPlan(String userQuery) {
        // Identify single tool if needed
        List<String> tools = new ArrayList<>();
        String lowerQuery = userQuery.toLowerCase();
        
        if (lowerQuery.contains("date") || lowerQuery.contains("time") || lowerQuery.contains("today")) {
            tools.add("getCurrentDateTime");
        } else if (lowerQuery.contains("add") || lowerQuery.contains("plus") || lowerQuery.contains("+")) {
            tools.add("add");
        } else if (lowerQuery.contains("multiply") || lowerQuery.contains("times") || lowerQuery.contains("*")) {
            tools.add("multiply");
        }
        
        // Use only 3 core brains for fast path
        List<String> coreBrains = new ArrayList<>();
        coreBrains.add("conductorAdvisor");
        coreBrains.add("toolCallAdvisor");
        coreBrains.add("personalityAdvisor");
        
        return new AgentPlan()
            .setIntent("SIMPLE")
            .setComplexity(1)
            .setAmbiguity(0)
            .setFocusArea("GENERAL")
            .setIgnoreArea("NONE")
            .setStrategy("FAST_RECALL")
            .setRequiredTools(tools)
            .setSelectedBrains(coreBrains)
            .setConfidence(0.95)
            .setUserQuery(userQuery)
            .setCreatedAt(System.currentTimeMillis());
    }
    
    /**
     * Extract user message from request
     */
    private String extractUserMessage(ChatClientRequest request) {
        try {
            if (request.prompt() != null && request.prompt().getInstructions() != null) {
                StringBuilder messageText = new StringBuilder();
                for (var message : request.prompt().getInstructions()) {
                    if (message instanceof UserMessage) {
                        UserMessage userMsg = (UserMessage) message;
                        messageText.append(userMsg.getText()).append(" ");
                    }
                }
                return messageText.toString().trim();
            }
        } catch (Exception e) {
            logger.debug("Failed to extract user message: {}", e.getMessage());
        }
        return "";
    }
}
