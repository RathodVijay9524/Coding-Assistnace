package com.vijay.editing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 💡 INLINE SUGGESTION ENGINE
 * 
 * Generates real-time inline suggestions as user types.
 * Provides method extraction, renaming, pattern, and bug fix suggestions.
 * 
 * ✅ PHASE 2: Intelligent Editing - Week 2
 */
@Service
@RequiredArgsConstructor
public class InlineSuggestionEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(InlineSuggestionEngine.class);
    private final ObjectMapper objectMapper;
    private final CodeSelectionAnalyzer codeSelectionAnalyzer;
    
    /**
     * Get inline suggestions at cursor position
     */
    @Tool(description = "Get inline suggestions at cursor position")
    public String getSuggestionsAtPosition(
            @ToolParam(description = "Full code") String fullCode,
            @ToolParam(description = "Cursor line") int cursorLine,
            @ToolParam(description = "Cursor column") int cursorColumn) {
        
        logger.info("💡 Getting inline suggestions at {}:{}", cursorLine, cursorColumn);
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Extract context around cursor
            String context = extractContext(fullCode, cursorLine, cursorColumn);
            
            // Generate suggestions
            List<InlineSuggestion> suggestions = new ArrayList<>();
            suggestions.addAll(suggestMethodExtractions(fullCode, context));
            suggestions.addAll(suggestVariableRenamings(fullCode, context));
            suggestions.addAll(suggestPatterns(fullCode, context));
            suggestions.addAll(suggestBugFixes(fullCode, context));
            
            // Sort by relevance
            suggestions.sort((a, b) -> Double.compare(b.getRelevance(), a.getRelevance()));
            
            result.put("status", "success");
            result.put("cursorPosition", Map.of("line", cursorLine, "column", cursorColumn));
            result.put("suggestions", suggestions);
            result.put("count", suggestions.size());
            
            logger.info("✅ Generated {} inline suggestions", suggestions.size());
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Inline suggestion failed: {}", e.getMessage());
            return errorResponse("Inline suggestion failed: " + e.getMessage());
        }
    }
    
    /**
     * Suggest method extractions
     */
    public List<InlineSuggestion> suggestMethodExtractions(String fullCode, String context) {
        logger.info("💡 Suggesting method extractions");
        
        List<InlineSuggestion> suggestions = new ArrayList<>();
        
        // Check if context has multiple statements
        int statementCount = countStatements(context);
        if (statementCount > 3) {
            InlineSuggestion suggestion = new InlineSuggestion();
            suggestion.setType("Extract Method");
            suggestion.setTitle("Extract this code block into a method");
            suggestion.setDescription("This code block can be extracted into a separate method for reusability");
            suggestion.setAction("extractMethod");
            suggestion.setRelevance(0.95);
            suggestion.setIcon("🔨");
            suggestion.setKeyboardShortcut("Ctrl+Alt+M");
            
            suggestions.add(suggestion);
        }
        
        return suggestions;
    }
    
    /**
     * Suggest variable renamings
     */
    public List<InlineSuggestion> suggestVariableRenamings(String fullCode, String context) {
        logger.info("💡 Suggesting variable renamings");
        
        List<InlineSuggestion> suggestions = new ArrayList<>();
        
        // Find poorly named variables
        Pattern varPattern = Pattern.compile("\\b([a-z])\\b");
        Matcher matcher = varPattern.matcher(context);
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            
            InlineSuggestion suggestion = new InlineSuggestion();
            suggestion.setType("Rename Variable");
            suggestion.setTitle("Rename '" + varName + "' to a more descriptive name");
            suggestion.setDescription("Single letter variable names reduce code clarity");
            suggestion.setAction("renameVariable");
            suggestion.setRelevance(0.85);
            suggestion.setIcon("✏️");
            suggestion.setKeyboardShortcut("Ctrl+Alt+R");
            
            suggestions.add(suggestion);
        }
        
        return suggestions;
    }
    
    /**
     * Suggest patterns
     */
    public List<InlineSuggestion> suggestPatterns(String fullCode, String context) {
        logger.info("💡 Suggesting patterns");
        
        List<InlineSuggestion> suggestions = new ArrayList<>();
        
        // Strategy pattern suggestion
        if (context.contains("if") && context.contains("else if")) {
            InlineSuggestion suggestion = new InlineSuggestion();
            suggestion.setType("Apply Strategy Pattern");
            suggestion.setTitle("Replace if-else with Strategy Pattern");
            suggestion.setDescription("Multiple if-else blocks can use Strategy pattern for better extensibility");
            suggestion.setAction("applyStrategyPattern");
            suggestion.setRelevance(0.8);
            suggestion.setIcon("🎯");
            suggestion.setKeyboardShortcut("Ctrl+Alt+P");
            
            suggestions.add(suggestion);
        }
        
        // Factory pattern suggestion
        if (context.contains("new ") && countOccurrences(context, "new ") > 2) {
            InlineSuggestion suggestion = new InlineSuggestion();
            suggestion.setType("Apply Factory Pattern");
            suggestion.setTitle("Use Factory Pattern for object creation");
            suggestion.setDescription("Multiple object creations can be centralized using Factory pattern");
            suggestion.setAction("applyFactoryPattern");
            suggestion.setRelevance(0.75);
            suggestion.setIcon("🏭");
            suggestion.setKeyboardShortcut("Ctrl+Alt+F");
            
            suggestions.add(suggestion);
        }
        
        return suggestions;
    }
    
    /**
     * Suggest bug fixes
     */
    public List<InlineSuggestion> suggestBugFixes(String fullCode, String context) {
        logger.info("💡 Suggesting bug fixes");
        
        List<InlineSuggestion> suggestions = new ArrayList<>();
        
        // Null pointer check
        if (context.contains(".") && !context.contains("null")) {
            InlineSuggestion suggestion = new InlineSuggestion();
            suggestion.setType("Add Null Check");
            suggestion.setTitle("Add null check before method call");
            suggestion.setDescription("Method call without null check may cause NullPointerException");
            suggestion.setAction("addNullCheck");
            suggestion.setRelevance(0.9);
            suggestion.setIcon("⚠️");
            suggestion.setKeyboardShortcut("Ctrl+Alt+N");
            
            suggestions.add(suggestion);
        }
        
        // Resource leak
        if (context.contains("new File") || context.contains("new Stream")) {
            InlineSuggestion suggestion = new InlineSuggestion();
            suggestion.setType("Fix Resource Leak");
            suggestion.setTitle("Use try-with-resources for resource management");
            suggestion.setDescription("Resources should be closed properly to prevent leaks");
            suggestion.setAction("fixResourceLeak");
            suggestion.setRelevance(0.88);
            suggestion.setIcon("🔒");
            suggestion.setKeyboardShortcut("Ctrl+Alt+L");
            
            suggestions.add(suggestion);
        }
        
        return suggestions;
    }
    
    /**
     * Get suggestions for specific code block
     */
    @Tool(description = "Get suggestions for specific code block")
    public String suggestForCodeBlock(
            @ToolParam(description = "Code block") String codeBlock,
            @ToolParam(description = "Block type") String blockType) {
        
        logger.info("💡 Getting suggestions for {} block", blockType);
        
        try {
            Map<String, Object> result = new HashMap<>();
            List<InlineSuggestion> suggestions = new ArrayList<>();
            
            if ("method".equalsIgnoreCase(blockType)) {
                suggestions.addAll(suggestMethodImprovements(codeBlock));
            } else if ("loop".equalsIgnoreCase(blockType)) {
                suggestions.addAll(suggestLoopOptimizations(codeBlock));
            } else if ("conditional".equalsIgnoreCase(blockType)) {
                suggestions.addAll(suggestConditionalImprovements(codeBlock));
            }
            
            result.put("status", "success");
            result.put("blockType", blockType);
            result.put("suggestions", suggestions);
            
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Block suggestion failed: {}", e.getMessage());
            return errorResponse("Block suggestion failed: " + e.getMessage());
        }
    }
    
    /**
     * Suggest method improvements
     */
    private List<InlineSuggestion> suggestMethodImprovements(String code) {
        List<InlineSuggestion> suggestions = new ArrayList<>();
        
        int lineCount = countLines(code);
        if (lineCount > 50) {
            suggestions.add(createSuggestion(
                "Long Method",
                "Method exceeds 50 lines",
                "Consider breaking into smaller methods",
                0.9
            ));
        }
        
        int complexity = countOccurrences(code, "if") + countOccurrences(code, "for");
        if (complexity > 10) {
            suggestions.add(createSuggestion(
                "High Complexity",
                "Method has high cyclomatic complexity",
                "Simplify logic or extract methods",
                0.85
            ));
        }
        
        return suggestions;
    }
    
    /**
     * Suggest loop optimizations
     */
    private List<InlineSuggestion> suggestLoopOptimizations(String code) {
        List<InlineSuggestion> suggestions = new ArrayList<>();
        
        if (code.contains("for (int i = 0")) {
            suggestions.add(createSuggestion(
                "Use Enhanced For Loop",
                "Traditional for loop can be replaced",
                "Use for-each loop for better readability",
                0.8
            ));
        }
        
        if (code.contains("while")) {
            suggestions.add(createSuggestion(
                "Optimize While Loop",
                "Consider using for loop instead",
                "For loops are more readable for iterations",
                0.7
            ));
        }
        
        return suggestions;
    }
    
    /**
     * Suggest conditional improvements
     */
    private List<InlineSuggestion> suggestConditionalImprovements(String code) {
        List<InlineSuggestion> suggestions = new ArrayList<>();
        
        int ifCount = countOccurrences(code, "if");
        if (ifCount > 5) {
            suggestions.add(createSuggestion(
                "Simplify Conditionals",
                "Too many if statements",
                "Consider using switch or strategy pattern",
                0.85
            ));
        }
        
        if (code.contains("&&") && code.contains("||")) {
            suggestions.add(createSuggestion(
                "Simplify Boolean Logic",
                "Complex boolean expression",
                "Extract to intermediate variables",
                0.75
            ));
        }
        
        return suggestions;
    }
    
    // Helper methods
    
    private String extractContext(String fullCode, int line, int column) {
        String[] lines = fullCode.split("\n");
        int startLine = Math.max(0, line - 5);
        int endLine = Math.min(lines.length, line + 5);
        
        StringBuilder context = new StringBuilder();
        for (int i = startLine; i < endLine; i++) {
            context.append(lines[i]).append("\n");
        }
        
        return context.toString();
    }
    
    private int countStatements(String code) {
        return countOccurrences(code, ";");
    }
    
    private int countLines(String code) {
        return code.split("\n").length;
    }
    
    private int countOccurrences(String code, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = code.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }
    
    private InlineSuggestion createSuggestion(String type, String title, String description, double relevance) {
        InlineSuggestion suggestion = new InlineSuggestion();
        suggestion.setType(type);
        suggestion.setTitle(title);
        suggestion.setDescription(description);
        suggestion.setRelevance(relevance);
        return suggestion;
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
        return "{\"status\": \"error\", \"message\": \"" + message.replace("\"", "\\\"") + "\"}";
    }
    
    // Inner class
    
    public static class InlineSuggestion {
        private String type;
        private String title;
        private String description;
        private String action;
        private double relevance;
        private String icon;
        private String keyboardShortcut;
        
        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        
        public double getRelevance() { return relevance; }
        public void setRelevance(double relevance) { this.relevance = relevance; }
        
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        
        public String getKeyboardShortcut() { return keyboardShortcut; }
        public void setKeyboardShortcut(String keyboardShortcut) { this.keyboardShortcut = keyboardShortcut; }
    }
}
