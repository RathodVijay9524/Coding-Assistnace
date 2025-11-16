package com.vijay.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.manager.AiToolProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ⚡ Performance Analysis Tool Service
 * 
 * Analyzes code performance including:
 * - Performance bottlenecks
 * - Algorithm complexity
 * - Memory usage patterns
 * - Caching opportunities
 * - Optimization suggestions
 * 
 * Implements AiToolProvider to be accessible from chatbot
 */
@Service
@RequiredArgsConstructor
public class PerformanceAnalysisToolService implements AiToolProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceAnalysisToolService.class);
    private final ObjectProvider<ChatClient> chatClientProvider;
    private final ObjectMapper objectMapper;
    
    /**
     * Analyze code performance
     */
    @Tool(description = "Analyze code performance and suggest optimizations")
    public String analyzePerformance(
            @ToolParam(description = "Code to analyze") String code,
            @ToolParam(description = "Programming language") String language,
            @ToolParam(description = "Analysis type (all/bottleneck/memory/algorithm/caching)") String analysisType) {
        
        logger.info("⚡ Starting performance analysis for: {}", analysisType);
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 1. Identify bottlenecks
            if ("bottleneck".equalsIgnoreCase(analysisType) || "all".equalsIgnoreCase(analysisType)) {
                result.put("bottlenecks", identifyBottlenecks(code));
            }
            
            // 2. Analyze algorithm complexity
            if ("algorithm".equalsIgnoreCase(analysisType) || "all".equalsIgnoreCase(analysisType)) {
                result.put("algorithmComplexity", analyzeAlgorithmComplexity(code));
            }
            
            // 3. Analyze memory usage
            if ("memory".equalsIgnoreCase(analysisType) || "all".equalsIgnoreCase(analysisType)) {
                result.put("memoryUsage", analyzeMemoryUsage(code));
            }
            
            // 4. Identify caching opportunities
            if ("caching".equalsIgnoreCase(analysisType) || "all".equalsIgnoreCase(analysisType)) {
                result.put("cachingOpportunities", identifyCachingOpportunities(code));
            }
            
            // 5. Get AI performance analysis
            result.put("aiAnalysis", getAIPerformanceAnalysis(code, language, analysisType));
            
            // 6. Suggest optimizations
            result.put("optimizations", suggestOptimizations(code, language));
            
            // 7. Estimate improvements
            result.put("improvements", estimateImprovements(result));
            
            // 8. Summary
            result.put("summary", generatePerformanceSummary(result));
            
            logger.info("✅ Performance analysis complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Performance analysis failed: {}", e.getMessage(), e);
            return errorResponse("Performance analysis failed: " + e.getMessage());
        }
    }
    
    /**
     * Identify performance bottlenecks
     */
    private List<Map<String, Object>> identifyBottlenecks(String code) {
        List<Map<String, Object>> bottlenecks = new ArrayList<>();
        
        try {
            String[] lines = code.split("\n");
            
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                
                // Check for nested loops
                int loopCount = countOccurrences(line, "for") + countOccurrences(line, "while");
                if (loopCount > 1) {
                    Map<String, Object> bottleneck = new HashMap<>();
                    bottleneck.put("line", i + 1);
                    bottleneck.put("type", "Nested Loops");
                    bottleneck.put("code", line.trim());
                    bottleneck.put("impact", "O(n²) or worse complexity");
                    bottleneck.put("suggestion", "Consider using data structures or algorithms to reduce nesting");
                    bottlenecks.add(bottleneck);
                }
                
                // Check for Thread.sleep
                if (line.contains("Thread.sleep")) {
                    Map<String, Object> bottleneck = new HashMap<>();
                    bottleneck.put("line", i + 1);
                    bottleneck.put("type", "Blocking Sleep");
                    bottleneck.put("code", line.trim());
                    bottleneck.put("impact", "Blocks thread execution");
                    bottleneck.put("suggestion", "Use async/await or scheduled executors");
                    bottlenecks.add(bottleneck);
                }
                
                // Check for synchronization
                if (line.contains("synchronized")) {
                    Map<String, Object> bottleneck = new HashMap<>();
                    bottleneck.put("line", i + 1);
                    bottleneck.put("type", "Synchronization");
                    bottleneck.put("code", line.trim());
                    bottleneck.put("impact", "Can cause contention");
                    bottleneck.put("suggestion", "Use concurrent data structures or fine-grained locking");
                    bottlenecks.add(bottleneck);
                }
                
                // Check for string concatenation in loops
                if ((line.contains("for") || line.contains("while")) && line.contains("+")) {
                    Map<String, Object> bottleneck = new HashMap<>();
                    bottleneck.put("line", i + 1);
                    bottleneck.put("type", "String Concatenation in Loop");
                    bottleneck.put("code", line.trim());
                    bottleneck.put("impact", "Creates new String objects repeatedly");
                    bottleneck.put("suggestion", "Use StringBuilder instead");
                    bottlenecks.add(bottleneck);
                }
            }
            
        } catch (Exception e) {
            logger.debug("Could not identify bottlenecks: {}", e.getMessage());
        }
        
        return bottlenecks;
    }
    
    /**
     * Analyze algorithm complexity
     */
    private Map<String, Object> analyzeAlgorithmComplexity(String code) {
        Map<String, Object> complexity = new HashMap<>();
        
        try {
            int forLoops = countOccurrences(code, "for");
            int whileLoops = countOccurrences(code, "while");
            int recursion = countOccurrences(code, "return ") + countOccurrences(code, "recursion");
            
            complexity.put("forLoops", forLoops);
            complexity.put("whileLoops", whileLoops);
            complexity.put("recursion", recursion);
            
            // Estimate complexity
            String estimatedComplexity;
            if (forLoops > 2) {
                estimatedComplexity = "O(n³) or worse";
            } else if (forLoops > 1) {
                estimatedComplexity = "O(n²)";
            } else if (forLoops > 0) {
                estimatedComplexity = "O(n)";
            } else if (recursion > 0) {
                estimatedComplexity = "O(2^n) - exponential (check for memoization)";
            } else {
                estimatedComplexity = "O(1)";
            }
            
            complexity.put("estimatedComplexity", estimatedComplexity);
            complexity.put("recommendation", getComplexityRecommendation(estimatedComplexity));
            
        } catch (Exception e) {
            logger.debug("Could not analyze complexity: {}", e.getMessage());
        }
        
        return complexity;
    }
    
    /**
     * Analyze memory usage
     */
    private Map<String, Object> analyzeMemoryUsage(String code) {
        Map<String, Object> memory = new HashMap<>();
        
        try {
            List<String> concerns = new ArrayList<>();
            
            // Check for large data structure allocations
            if (code.contains("new ArrayList") || code.contains("new HashMap")) {
                if (code.contains("for") || code.contains("while")) {
                    concerns.add("⚠️ Creating data structures in loops may cause memory issues");
                }
            }
            
            // Check for recursive calls
            if (countOccurrences(code, "recursion") > 0 || countOccurrences(code, "return ") > 5) {
                concerns.add("⚠️ Deep recursion may cause stack overflow");
            }
            
            // Check for string operations
            if (code.contains("substring") || code.contains("split")) {
                concerns.add("⚠️ String operations create new objects");
            }
            
            // Check for caching
            if (!code.contains("cache") && !code.contains("memo")) {
                concerns.add("⚠️ No caching detected - consider memoization");
            }
            
            memory.put("concerns", concerns);
            memory.put("severity", concerns.isEmpty() ? "LOW" : "MEDIUM");
            
        } catch (Exception e) {
            logger.debug("Could not analyze memory: {}", e.getMessage());
        }
        
        return memory;
    }
    
    /**
     * Identify caching opportunities
     */
    private List<String> identifyCachingOpportunities(String code) {
        List<String> opportunities = new ArrayList<>();
        
        try {
            // Check for repeated calculations
            if (countOccurrences(code, "calculate") > 1 || countOccurrences(code, "compute") > 1) {
                opportunities.add("💾 Repeated calculations detected - use memoization");
            }
            
            // Check for database queries
            if (code.contains("query") || code.contains("select")) {
                opportunities.add("💾 Database queries detected - consider query result caching");
            }
            
            // Check for API calls
            if (code.contains("http") || code.contains("api")) {
                opportunities.add("💾 API calls detected - implement response caching");
            }
            
            // Check for expensive operations
            if (code.contains("sort") || code.contains("search")) {
                opportunities.add("💾 Expensive operations detected - cache results if possible");
            }
            
        } catch (Exception e) {
            logger.debug("Could not identify caching opportunities: {}", e.getMessage());
        }
        
        return opportunities;
    }
    
    /**
     * Get AI performance analysis
     */
    private List<String> getAIPerformanceAnalysis(String code, String language, String analysisType) {
        List<String> analysis = new ArrayList<>();
        
        try {
            String prompt = String.format("""
                Analyze the performance of this %s code (focus: %s):
                
                ```%s
                %s
                ```
                
                Provide 3-5 performance analysis points.
                Format as numbered list.
                """, language, analysisType, language, code);
            
            String aiResponse = chatClientProvider.getObject().prompt()
                .user(prompt)
                .call()
                .content();
            
            // Parse analysis
            String[] lines = aiResponse.split("\n");
            for (String line : lines) {
                if (line.matches("^\\d+\\..*")) {
                    analysis.add(line.trim());
                }
            }
            
        } catch (Exception e) {
            logger.debug("Could not get AI analysis: {}", e.getMessage());
            analysis.add("Unable to generate AI analysis at this time");
        }
        
        return analysis;
    }
    
    /**
     * Suggest optimizations
     */
    private List<String> suggestOptimizations(String code, String language) {
        List<String> optimizations = new ArrayList<>();
        
        try {
            String prompt = String.format("""
                Suggest performance optimizations for this %s code:
                
                ```%s
                %s
                ```
                
                Provide 3-5 specific optimization suggestions with estimated improvements.
                """, language, language, code);
            
            String aiResponse = chatClientProvider.getObject().prompt()
                .user(prompt)
                .call()
                .content();
            
            optimizations.add(aiResponse);
            
        } catch (Exception e) {
            logger.debug("Could not suggest optimizations: {}", e.getMessage());
            optimizations.add("Unable to generate optimizations at this time");
        }
        
        return optimizations;
    }
    
    /**
     * Estimate improvements
     */
    private Map<String, Object> estimateImprovements(Map<String, Object> result) {
        Map<String, Object> improvements = new HashMap<>();
        
        try {
            List<Map<String, Object>> bottlenecks = (List<Map<String, Object>>) result.get("bottlenecks");
            int bottleneckCount = bottlenecks != null ? bottlenecks.size() : 0;
            
            // Estimate performance improvement
            int estimatedImprovement = bottleneckCount * 10;
            
            improvements.put("estimatedImprovement", Math.min(estimatedImprovement, 80) + "%");
            improvements.put("bottleneckCount", bottleneckCount);
            improvements.put("priority", bottleneckCount > 3 ? "HIGH" : "MEDIUM");
            
        } catch (Exception e) {
            logger.debug("Could not estimate improvements: {}", e.getMessage());
        }
        
        return improvements;
    }
    
    /**
     * Generate performance summary
     */
    private String generatePerformanceSummary(Map<String, Object> result) {
        try {
            Map<String, Object> improvements = (Map<String, Object>) result.get("improvements");
            String estimated = (String) improvements.getOrDefault("estimatedImprovement", "Unknown");
            
            return "Performance analysis complete. Estimated improvement potential: " + estimated;
            
        } catch (Exception e) {
            return "Performance analysis completed";
        }
    }
    
    // ============ Helper Methods ============
    
    private String getComplexityRecommendation(String complexity) {
        if (complexity.contains("O(1)")) {
            return "✅ Excellent - Constant time complexity";
        } else if (complexity.contains("O(n)")) {
            return "✅ Good - Linear time complexity";
        } else if (complexity.contains("O(n²)")) {
            return "⚠️ Fair - Quadratic complexity, consider optimization";
        } else if (complexity.contains("O(2^n)")) {
            return "🔴 Poor - Exponential complexity, optimization critical";
        }
        return "Review complexity";
    }
    
    private int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }
    
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("❌ JSON serialization failed: {}", e.getMessage());
            return "{\"error\": \"JSON serialization failed\"}";
        }
    }
    
    private String errorResponse(String message) {
        return "{\"error\": \"" + message.replace("\"", "\\\"") + "\"}";
    }
}
