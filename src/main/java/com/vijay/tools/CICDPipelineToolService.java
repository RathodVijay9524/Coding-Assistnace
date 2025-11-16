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
 * 🚀 CI/CD Pipeline Tool Service
 * 
 * Generates and manages CI/CD pipeline configurations including:
 * - GitHub Actions workflows
 * - GitLab CI/CD pipelines
 * - Jenkins pipelines
 * - Build automation
 * - Testing automation
 * - Deployment automation
 * - Release management
 * 
 * Implements AiToolProvider to be accessible from chatbot
 */
@Service
@RequiredArgsConstructor
public class CICDPipelineToolService implements AiToolProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(CICDPipelineToolService.class);
    private final ObjectProvider<ChatClient> chatClientProvider;
    private final ObjectMapper objectMapper;
    
    /**
     * Generate CI/CD pipeline configuration
     */
    @Tool(description = "Generate CI/CD pipeline configuration (GitHub Actions, GitLab CI, Jenkins)")
    public String generatePipelineConfig(
            @ToolParam(description = "Platform (github/gitlab/jenkins)") String platform,
            @ToolParam(description = "Build tool (maven/gradle/npm)") String buildTool,
            @ToolParam(description = "Deployment target (docker/kubernetes/cloud)") String deploymentTarget) {
        
        logger.info("🚀 Generating CI/CD pipeline for: {}", platform);
        
        try {
            String prompt = String.format("""
                Generate a complete CI/CD pipeline configuration for %s using %s:
                
                Build Tool: %s
                Deployment Target: %s
                
                Include:
                - Trigger conditions (push, PR, tags)
                - Build stage with caching
                - Unit test execution
                - Integration test execution
                - Code quality analysis (SonarQube)
                - Security scanning
                - Docker image build and push
                - Deployment to %s
                - Notifications (Slack, email)
                - Rollback strategy
                - Performance monitoring
                
                Format as proper %s configuration with detailed comments.
                """, platform, buildTool, buildTool, deploymentTarget, deploymentTarget, 
                    platform.equals("github") ? "YAML (.github/workflows)" : 
                    platform.equals("gitlab") ? "YAML (.gitlab-ci.yml)" : "Groovy (Jenkinsfile)");
            
            String config = chatClientProvider.getObject().prompt()
                .user(prompt)
                .call()
                .content();
            
            Map<String, Object> result = new HashMap<>();
            result.put("pipeline", config);
            result.put("platform", platform);
            result.put("buildTool", buildTool);
            result.put("deploymentTarget", deploymentTarget);
            
            logger.info("✅ CI/CD pipeline generated");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ CI/CD pipeline generation failed: {}", e.getMessage());
            return errorResponse("Pipeline generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate build stage configuration
     */
    @Tool(description = "Generate build stage configuration with caching and optimization")
    public String generateBuildStage(
            @ToolParam(description = "Build tool (maven/gradle/npm)") String buildTool,
            @ToolParam(description = "Language (java/nodejs/python)") String language,
            @ToolParam(description = "Optimization focus (speed/size/security)") String focus) {
        
        logger.info("🚀 Generating build stage for: {}", buildTool);
        
        try {
            String prompt = String.format("""
                Generate an optimized build stage configuration for %s (%s):
                
                Optimization Focus: %s
                
                Include:
                - Dependency caching strategy
                - Parallel build configuration
                - Build optimization flags
                - Artifact generation
                - Build artifact caching
                - Error handling
                - Build timeout configuration
                - Resource limits
                - Build logging
                - Failure notifications
                
                Provide configuration for common CI/CD platforms.
                """, buildTool, language, focus);
            
            String buildStage = chatClientProvider.getObject().prompt()
                .user(prompt)
                .call()
                .content();
            
            Map<String, Object> result = new HashMap<>();
            result.put("buildStage", buildStage);
            result.put("buildTool", buildTool);
            result.put("focus", focus);
            
            logger.info("✅ Build stage generated");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Build stage generation failed: {}", e.getMessage());
            return errorResponse("Build stage generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate test stage configuration
     */
    @Tool(description = "Generate automated testing stage configuration")
    public String generateTestStage(
            @ToolParam(description = "Test types (unit/integration/e2e/all)") String testTypes,
            @ToolParam(description = "Test framework (JUnit/TestNG/Pytest/Jest)") String framework,
            @ToolParam(description = "Coverage threshold (%)") String coverageThreshold) {
        
        logger.info("🚀 Generating test stage");
        
        try {
            String prompt = String.format("""
                Generate a comprehensive test stage configuration:
                
                Test Types: %s
                Framework: %s
                Coverage Threshold: %s%%
                
                Include:
                - Unit test execution
                - Integration test execution
                - E2E test execution (if applicable)
                - Code coverage analysis
                - Coverage threshold enforcement
                - Test result reporting
                - Test artifact archiving
                - Parallel test execution
                - Test timeout configuration
                - Failure handling
                - Performance test execution
                - Security test execution
                
                Provide configuration with best practices.
                """, testTypes, framework, coverageThreshold);
            
            String testStage = chatClientProvider.getObject().prompt()
                .user(prompt)
                .call()
                .content();
            
            Map<String, Object> result = new HashMap<>();
            result.put("testStage", testStage);
            result.put("testTypes", testTypes);
            result.put("framework", framework);
            result.put("coverageThreshold", coverageThreshold);
            
            logger.info("✅ Test stage generated");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Test stage generation failed: {}", e.getMessage());
            return errorResponse("Test stage generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Generate deployment stage configuration
     */
    @Tool(description = "Generate deployment stage configuration with rollback strategy")
    public String generateDeploymentStage(
            @ToolParam(description = "Deployment target (docker/kubernetes/cloud)") String target,
            @ToolParam(description = "Environment (dev/staging/production)") String environment,
            @ToolParam(description = "Deployment strategy (blue-green/canary/rolling)") String strategy) {
        
        logger.info("🚀 Generating deployment stage for: {}", target);
        
        try {
            String prompt = String.format("""
                Generate a deployment stage configuration:
                
                Target: %s
                Environment: %s
                Strategy: %s
                
                Include:
                - Pre-deployment validation
                - %s deployment commands
                - Health checks
                - Smoke tests
                - %s deployment strategy implementation
                - Rollback procedure
                - Post-deployment verification
                - Monitoring setup
                - Alert configuration
                - Deployment notifications
                - Approval gates (for production)
                - Deployment logging
                
                Provide production-ready configuration.
                """, target, environment, strategy, target, strategy);
            
            String deploymentStage = chatClientProvider.getObject().prompt()
                .user(prompt)
                .call()
                .content();
            
            Map<String, Object> result = new HashMap<>();
            result.put("deploymentStage", deploymentStage);
            result.put("target", target);
            result.put("environment", environment);
            result.put("strategy", strategy);
            
            logger.info("✅ Deployment stage generated");
            return toJson(result);
            
        } catch (Exception e) {
            logger.error("❌ Deployment stage generation failed: {}", e.getMessage());
            return errorResponse("Deployment stage generation failed: " + e.getMessage());
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
