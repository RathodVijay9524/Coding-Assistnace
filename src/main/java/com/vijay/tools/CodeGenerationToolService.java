package com.vijay.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vijay.manager.AiToolProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 💻 Code Generation Tool Service
 * 
 * Generates code in any programming language based on:
 * - Description of what code should do
 * - Programming language
 * - Context and requirements
 * 
 * Implements AiToolProvider to be accessible from chatbot
 */
@Service
@RequiredArgsConstructor
public class CodeGenerationToolService implements AiToolProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(CodeGenerationToolService.class);
    private final ObjectProvider<ChatClient> chatClientProvider;
    private final ObjectMapper objectMapper;
    
    /**
     * Generate code from description
     */
    @Tool(description = "Generate code in specified language from description")
    public String generateCode(
            @ToolParam(description = "Programming language (Java, Python, JavaScript, etc.)") String language,
            @ToolParam(description = "Description of what the code should do") String description,
            @ToolParam(description = "Additional context or requirements") String context) {
        
        logger.info("💻 Generating {} code for: {}", language, description);
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Build prompt for code generation
            String prompt = buildCodeGenerationPrompt(language, description, context);
            
            // Call AI to generate code
            String generatedCode = chatClientProvider.getObject().prompt()
                .user(prompt)
                .call()
                .content();
            
            result.put("language", language);
            result.put("description", description);
            result.put("code", generatedCode);
            result.put("explanation", generateExplanation(language, description, generatedCode));
            result.put("status", "success");
            
            logger.info("✅ Code generation complete for {}", language);
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Code generation failed: {}", e.getMessage(), e);
            return errorResponse("Code generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate boilerplate project
     */
    @Tool(description = "Generate project boilerplate with directory structure and initial files")
    public String generateBoilerplate(
            @ToolParam(description = "Project type (SpringBoot, React, Django, etc.)") String projectType,
            @ToolParam(description = "Project name") String projectName,
            @ToolParam(description = "Additional features to include") String features) {
        
        logger.info("📦 Generating {} boilerplate for: {}", projectType, projectName);
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            String prompt = buildBoilerplatePrompt(projectType, projectName, features);
            
            String boilerplate = chatClientProvider.getObject().prompt()
                .user(prompt)
                .call()
                .content();
            
            result.put("projectType", projectType);
            result.put("projectName", projectName);
            result.put("structure", boilerplate);
            result.put("status", "success");
            
            logger.info("✅ Boilerplate generation complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Boilerplate generation failed: {}", e.getMessage());
            return errorResponse("Boilerplate generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate specific component
     */
    @Tool(description = "Generate a specific component (Controller, Service, Model, etc.)")
    public String generateComponent(
            @ToolParam(description = "Component type (Controller, Service, Model, etc.)") String componentType,
            @ToolParam(description = "Component name") String componentName,
            @ToolParam(description = "Framework/Language") String framework,
            @ToolParam(description = "Functionality description") String functionality) {
        
        logger.info("🔧 Generating {} component: {}", componentType, componentName);
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            String prompt = buildComponentPrompt(componentType, componentName, framework, functionality);
            
            String component = chatClientProvider.getObject().prompt()
                .user(prompt)
                .call()
                .content();
            
            result.put("componentType", componentType);
            result.put("componentName", componentName);
            result.put("framework", framework);
            result.put("code", component);
            result.put("status", "success");
            
            logger.info("✅ Component generation complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Component generation failed: {}", e.getMessage());
            return errorResponse("Component generation failed: " + e.getMessage());
        }
    }
    
    // ============ Helper Methods ============
    
    private String buildCodeGenerationPrompt(String language, String description, String context) {
        return String.format("""
            Generate %s code for the following:
            
            Description: %s
            
            Context: %s
            
            Requirements:
            - Write clean, well-documented code
            - Follow %s best practices
            - Include error handling
            - Add comments for complex logic
            - Make the code production-ready
            
            Return ONLY the code, no explanations.
            """, language, description, context, language);
    }
    
    private String buildBoilerplatePrompt(String projectType, String projectName, String features) {
        return String.format("""
            Generate a %s project boilerplate for "%s" with the following features:
            
            Features: %s
            
            Include:
            - Directory structure
            - Configuration files
            - Main entry point
            - Example code
            - Dependencies/requirements
            - README with setup instructions
            
            Format as a structured guide with file paths and content.
            """, projectType, projectName, features);
    }
    
    private String buildComponentPrompt(String componentType, String componentName, String framework, String functionality) {
        return String.format("""
            Generate a %s component named "%s" for %s framework.
            
            Functionality: %s
            
            Requirements:
            - Follow %s conventions
            - Include proper annotations/decorators
            - Add error handling
            - Include documentation
            - Make it reusable and testable
            
            Return ONLY the code.
            """, componentType, componentName, framework, functionality, framework);
    }
    
    private String generateExplanation(String language, String description, String code) {
        return String.format("""
            Generated %s code for: %s
            
            Key points:
            - Language: %s
            - Purpose: %s
            - Lines of code: %d
            - Includes error handling and documentation
            """, language, description, language, description, code.split("\n").length);
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
