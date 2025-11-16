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
 * 🐳 Docker Configuration Tool Service
 * 
 * Generates and manages Docker configurations including:
 * - Dockerfile generation
 * - Docker Compose configurations
 * - Multi-stage builds
 * - Container optimization
 * - Security best practices
 * - Performance tuning
 * 
 * Implements AiToolProvider to be accessible from chatbot
 */
@Service
@RequiredArgsConstructor
public class DockerConfigToolService implements AiToolProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(DockerConfigToolService.class);
    private final ObjectProvider<ChatClient> chatClientProvider;
    private final ObjectMapper objectMapper;
    
    /**
     * Generate Dockerfile
     */
    @Tool(description = "Generate optimized Dockerfile for Spring Boot applications")
    public String generateDockerfile(
            @ToolParam(description = "Application type (spring-boot/java/nodejs/python)") String appType,
            @ToolParam(description = "Base image preference (alpine/slim/full)") String baseImage,
            @ToolParam(description = "Application details (name, version, port)") String appDetails) {
        
        logger.info("🐳 Generating Dockerfile for: {}", appType);
        
        try {
            String prompt = String.format("""
                Generate an optimized Dockerfile for a %s application:
                
                Application Details:
                %s
                
                Base Image Preference: %s
                
                Include:
                - Multi-stage build for optimization
                - Minimal final image size
                - Security best practices
                - Health check configuration
                - Proper signal handling
                - Non-root user execution
                - Layer caching optimization
                - Build arguments for flexibility
                
                Format as Dockerfile with detailed comments.
                """, appType, appDetails, baseImage);
            
            String dockerfile = chatClientProvider.getObject().prompt()
                .user(prompt)
                .call()
                .content();
            
            Map<String, Object> result = new HashMap<>();
            result.put("dockerfile", dockerfile);
            result.put("appType", appType);
            result.put("baseImage", baseImage);
            
            logger.info("✅ Dockerfile generated");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Dockerfile generation failed: {}", e.getMessage());
            return errorResponse("Dockerfile generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate Docker Compose configuration
     */
    @Tool(description = "Generate Docker Compose configuration for multi-container applications")
    public String generateDockerCompose(
            @ToolParam(description = "Services needed (app/db/cache/queue/all)") String services,
            @ToolParam(description = "Database type (MySQL/PostgreSQL/MongoDB)") String dbType,
            @ToolParam(description = "Environment (development/staging/production)") String environment) {
        
        logger.info("🐳 Generating Docker Compose for: {}", services);
        
        try {
            String prompt = String.format("""
                Generate a Docker Compose configuration for %s environment:
                
                Services: %s
                Database: %s
                
                Include:
                - Application service
                - Database service (if needed)
                - Cache service (if needed)
                - Message queue (if needed)
                - Volume configurations
                - Network setup
                - Environment variables
                - Health checks
                - Resource limits
                - Logging configuration
                - Restart policies
                
                Format as docker-compose.yml with comments.
                """, environment, services, dbType);
            
            String compose = chatClientProvider.getObject().prompt()
                .user(prompt)
                .call()
                .content();
            
            Map<String, Object> result = new HashMap<>();
            result.put("dockerCompose", compose);
            result.put("services", services);
            result.put("dbType", dbType);
            result.put("environment", environment);
            
            logger.info("✅ Docker Compose generated");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Docker Compose generation failed: {}", e.getMessage());
            return errorResponse("Docker Compose generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Optimize Docker image
     */
    @Tool(description = "Analyze and optimize Docker image for size and performance")
    public String optimizeDockerImage(
            @ToolParam(description = "Current Dockerfile") String dockerfile,
            @ToolParam(description = "Optimization focus (size/speed/security/all)") String focus,
            @ToolParam(description = "Application type") String appType) {
        
        logger.info("🐳 Optimizing Docker image for: {}", focus);
        
        try {
            String prompt = String.format("""
                Analyze and optimize this %s Dockerfile focusing on %s:
                
                ```dockerfile
                %s
                ```
                
                Provide:
                1. Current image size estimate
                2. Optimization opportunities
                3. Security improvements
                4. Performance enhancements
                5. Best practices recommendations
                6. Optimized Dockerfile
                
                Include estimated size reduction and performance improvements.
                """, appType, focus, dockerfile);
            
            String optimization = chatClientProvider.getObject().prompt()
                .user(prompt)
                .call()
                .content();
            
            Map<String, Object> result = new HashMap<>();
            result.put("optimization", optimization);
            result.put("focus", focus);
            
            logger.info("✅ Docker image optimization complete");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Docker image optimization failed: {}", e.getMessage());
            return errorResponse("Optimization failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate Docker build script
     */
    @Tool(description = "Generate Docker build and deployment scripts")
    public String generateBuildScript(
            @ToolParam(description = "Image name and tag") String imageName,
            @ToolParam(description = "Registry (Docker Hub/ECR/GCR)") String registry,
            @ToolParam(description = "Build options (push/scan/sign)") String options) {
        
        logger.info("🐳 Generating Docker build script");
        
        try {
            String prompt = String.format("""
                Generate a Docker build and deployment script:
                
                Image: %s
                Registry: %s
                Options: %s
                
                Include:
                - Build command with build arguments
                - Tag management
                - Registry push commands
                - Security scanning (if enabled)
                - Image signing (if enabled)
                - Error handling
                - Logging
                - Cleanup commands
                
                Format as shell script (.sh) with detailed comments.
                """, imageName, registry, options);
            
            String script = chatClientProvider.getObject().prompt()
                .user(prompt)
                .call()
                .content();
            
            Map<String, Object> result = new HashMap<>();
            result.put("script", script);
            result.put("imageName", imageName);
            result.put("registry", registry);
            
            logger.info("✅ Build script generated");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Build script generation failed: {}", e.getMessage());
            return errorResponse("Build script generation failed: " + e.getMessage());
        }
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
