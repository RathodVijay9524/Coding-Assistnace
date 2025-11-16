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
 * 🔄 Refactoring Tool Service
 * 
 * Suggests refactoring improvements including:
 * - Design pattern recommendations
 * - Code duplication detection
 * - Method extraction suggestions
 * - Class reorganization
 * - Naming improvements
 * 
 * Implements AiToolProvider to be accessible from chatbot
 */
@Service
@RequiredArgsConstructor
public class RefactoringToolService implements AiToolProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(RefactoringToolService.class);
    private final ObjectProvider<ChatClient> chatClientProvider;
    private final ObjectMapper objectMapper;
    
    /**
     * Suggest refactoring improvements
     */
    @Tool(description = "Suggest refactoring improvements for code")
    public String suggestRefactoring(
            @ToolParam(description = "Code to refactor") String code,
            @ToolParam(description = "Focus area (design/duplication/extraction/naming/all)") String focusArea,
            @ToolParam(description = "Programming language") String language) {
        
        logger.info("🔄 Starting refactoring analysis for: {}", focusArea);
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 1. Analyze code structure
            result.put("structure", analyzeCodeStructure(code, language));
            
            // 2. Detect design pattern opportunities
            if ("design".equalsIgnoreCase(focusArea) || "all".equalsIgnoreCase(focusArea)) {
                result.put("designPatterns", suggestDesignPatterns(code, language));
            }
            
            // 3. Detect code duplication
            if ("duplication".equalsIgnoreCase(focusArea) || "all".equalsIgnoreCase(focusArea)) {
                result.put("duplication", detectDuplication(code));
            }
            
            // 4. Suggest method extraction
            if ("extraction".equalsIgnoreCase(focusArea) || "all".equalsIgnoreCase(focusArea)) {
                result.put("methodExtraction", suggestMethodExtraction(code, language));
            }
            
            // 5. Suggest naming improvements
            if ("naming".equalsIgnoreCase(focusArea) || "all".equalsIgnoreCase(focusArea)) {
                result.put("namingImprovements", suggestNamingImprovements(code));
            }
            
            // 6. Get AI suggestions
            result.put("aiSuggestions", getAIRefactoringSuggestions(code, language, focusArea));
            
            // 7. Generate refactored code
            result.put("refactoredCode", generateRefactoredCode(code, language, focusArea));
            
            // 8. Summary
            result.put("summary", generateRefactoringSummary(result));
            
            logger.info("✅ Refactoring analysis complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Refactoring analysis failed: {}", e.getMessage(), e);
            return errorResponse("Refactoring analysis failed: " + e.getMessage());
        }
    }
    
    /**
     * Analyze code structure
     */
    private Map<String, Object> analyzeCodeStructure(String code, String language) {
        Map<String, Object> structure = new HashMap<>();
        
        try {
            String[] lines = code.split("\n");
            structure.put("totalLines", lines.length);
            structure.put("methods", countOccurrences(code, "public ") + countOccurrences(code, "def "));
            structure.put("classes", countOccurrences(code, "class "));
            structure.put("complexity", calculateComplexity(code));
            
        } catch (Exception e) {
            logger.debug("Could not analyze structure: {}", e.getMessage());
        }
        
        return structure;
    }
    
    /**
     * Suggest design patterns
     */
    private List<String> suggestDesignPatterns(String code, String language) {
        List<String> patterns = new ArrayList<>();
        
        try {
            // Check for pattern opportunities
            if (code.contains("if") && code.contains("else if") && countOccurrences(code, "if") > 3) {
                patterns.add("💡 Strategy Pattern - Multiple conditional branches can use Strategy pattern");
            }
            
            if (code.contains("new ") && countOccurrences(code, "new ") > 5) {
                patterns.add("💡 Factory Pattern - Multiple object creations can use Factory pattern");
            }
            
            if (code.contains("public static") && countOccurrences(code, "public static") > 3) {
                patterns.add("💡 Singleton Pattern - Consider using Singleton for shared state");
            }
            
            if (code.contains("interface") || code.contains("implements")) {
                patterns.add("💡 Decorator Pattern - Can add functionality without modifying original");
            }
            
            if (code.contains("observer") || code.contains("listener")) {
                patterns.add("💡 Observer Pattern - Already using, ensure proper implementation");
            }
            
        } catch (Exception e) {
            logger.debug("Could not suggest patterns: {}", e.getMessage());
        }
        
        return patterns;
    }
    
    /**
     * Detect code duplication
     */
    private Map<String, Object> detectDuplication(String code) {
        Map<String, Object> duplication = new HashMap<>();
        List<String> duplicates = new ArrayList<>();
        
        try {
            String[] lines = code.split("\n");
            
            // Simple duplication detection
            for (int i = 0; i < lines.length - 2; i++) {
                for (int j = i + 3; j < lines.length; j++) {
                    if (lines[i].trim().equals(lines[j].trim()) && !lines[i].trim().isEmpty()) {
                        duplicates.add("Lines " + (i + 1) + " and " + (j + 1) + " are identical");
                    }
                }
            }
            
            duplication.put("count", duplicates.size());
            duplication.put("duplicates", duplicates);
            duplication.put("severity", duplicates.isEmpty() ? "LOW" : "MEDIUM");
            
        } catch (Exception e) {
            logger.debug("Could not detect duplication: {}", e.getMessage());
        }
        
        return duplication;
    }
    
    /**
     * Suggest method extraction
     */
    private List<String> suggestMethodExtraction(String code, String language) {
        List<String> suggestions = new ArrayList<>();
        
        try {
            String[] lines = code.split("\n");
            
            // Find long methods
            if (lines.length > 50) {
                suggestions.add("📌 Extract long method into smaller methods (current: " + lines.length + " lines)");
            }
            
            // Find nested blocks
            int maxNesting = calculateMaxNesting(code);
            if (maxNesting > 3) {
                suggestions.add("📌 Extract nested blocks into separate methods (nesting level: " + maxNesting + ")");
            }
            
            // Find repeated patterns
            if (countOccurrences(code, "try") > 2) {
                suggestions.add("📌 Extract common try-catch patterns into utility methods");
            }
            
            if (countOccurrences(code, "for") > 2) {
                suggestions.add("📌 Extract loop logic into separate methods");
            }
            
        } catch (Exception e) {
            logger.debug("Could not suggest extraction: {}", e.getMessage());
        }
        
        return suggestions;
    }
    
    /**
     * Suggest naming improvements
     */
    private List<String> suggestNamingImprovements(String code) {
        List<String> suggestions = new ArrayList<>();
        
        try {
            // Check for single letter variables
            if (code.matches(".*\\b[a-z]\\b.*")) {
                suggestions.add("📝 Avoid single-letter variable names (use descriptive names)");
            }
            
            // Check for abbreviations
            if (code.contains("tmp") || code.contains("temp") || code.contains("var")) {
                suggestions.add("📝 Replace abbreviations with full names (tmp → temporary, var → variable)");
            }
            
            // Check for unclear names
            if (code.contains("data") || code.contains("value") || code.contains("result")) {
                suggestions.add("📝 Use more specific names instead of generic ones (data → userData, value → count)");
            }
            
            // Check for boolean naming
            if (code.contains("is") || code.contains("has") || code.contains("can")) {
                suggestions.add("✅ Good: Boolean variables use is/has/can prefix");
            }
            
        } catch (Exception e) {
            logger.debug("Could not suggest naming: {}", e.getMessage());
        }
        
        return suggestions;
    }
    
    /**
     * Get AI refactoring suggestions
     */
    private List<String> getAIRefactoringSuggestions(String code, String language, String focusArea) {
        List<String> suggestions = new ArrayList<>();
        
        try {
            String prompt = String.format("""
                Review this %s code and suggest refactoring improvements focusing on %s:
                
                ```%s
                %s
                ```
                
                Provide 3-5 specific, actionable refactoring suggestions.
                Format as a numbered list.
                """, language, focusArea, language, code);
            
            String aiResponse = chatClientProvider.getObject().prompt()
                .user(prompt)
                .call()
                .content();
            
            // Parse suggestions
            String[] lines = aiResponse.split("\n");
            for (String line : lines) {
                if (line.matches("^\\d+\\..*")) {
                    suggestions.add(line.trim());
                }
            }
            
        } catch (Exception e) {
            logger.debug("Could not get AI suggestions: {}", e.getMessage());
            suggestions.add("Unable to generate AI suggestions at this time");
        }
        
        return suggestions;
    }
    
    /**
     * Generate refactored code
     */
    private String generateRefactoredCode(String code, String language, String focusArea) {
        try {
            String prompt = String.format("""
                Refactor this %s code focusing on %s:
                
                ```%s
                %s
                ```
                
                Return ONLY the refactored code, no explanations.
                """, language, focusArea, language, code);
            
            return chatClientProvider.getObject().prompt()
                .user(prompt)
                .call()
                .content();
                
        } catch (Exception e) {
            logger.debug("Could not generate refactored code: {}", e.getMessage());
            return "// Refactoring failed: " + e.getMessage();
        }
    }
    
    /**
     * Generate refactoring summary
     */
    private String generateRefactoringSummary(Map<String, Object> result) {
        try {
            List<String> patterns = (List<String>) result.get("designPatterns");
            Map<String, Object> duplication = (Map<String, Object>) result.get("duplication");
            
            int patternCount = patterns != null ? patterns.size() : 0;
            int dupCount = duplication != null ? ((Number) duplication.getOrDefault("count", 0)).intValue() : 0;
            
            if (patternCount > 0 || dupCount > 0) {
                return String.format("Found %d design pattern opportunities and %d duplications. Refactoring recommended.", patternCount, dupCount);
            } else {
                return "Code structure is good. Minor improvements suggested.";
            }
            
        } catch (Exception e) {
            return "Refactoring analysis completed";
        }
    }
    
    // ============ Helper Methods ============
    
    private int calculateComplexity(String code) {
        int complexity = 0;
        complexity += countOccurrences(code, "if");
        complexity += countOccurrences(code, "for");
        complexity += countOccurrences(code, "while");
        complexity += countOccurrences(code, "switch");
        return Math.min(complexity, 10);
    }
    
    private int calculateMaxNesting(String code) {
        int maxNesting = 0;
        int currentNesting = 0;
        
        for (char c : code.toCharArray()) {
            if (c == '{') {
                currentNesting++;
                maxNesting = Math.max(maxNesting, currentNesting);
            } else if (c == '}') {
                currentNesting--;
            }
        }
        
        return maxNesting;
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
