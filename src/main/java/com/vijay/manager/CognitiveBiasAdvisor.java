package com.vijay.manager;

import com.vijay.dto.CognitiveBias;
import com.vijay.service.CognitiveBiasEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

/**
 * 🧠 Brain 10: Cognitive Bias Advisor
 * 
 * Purpose: Simulate human-like cognitive biases in decision-making
 * 
 * Responsibilities:
 * - Apply cognitive biases to responses
 * - Make AI think more like humans
 * - Simulate natural thinking patterns
 * - Track and rotate biases
 * - Record interaction history
 * 
 * Execution Order: 850 (Late, after personality but before final evaluation)
 */
@Component
public class CognitiveBiasAdvisor implements CallAdvisor {
    
    private static final Logger logger = LoggerFactory.getLogger(CognitiveBiasAdvisor.class);
    
    private final CognitiveBiasEngine biasEngine;
    
    public CognitiveBiasAdvisor(CognitiveBiasEngine biasEngine) {
        this.biasEngine = biasEngine;
    }
    
    @Override
    public String getName() {
        return "CognitiveBiasAdvisor";
    }
    
    @Override
    public int getOrder() {
        return 850; // Execute late, after personality
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("🧠 Brain 10 (Cognitive Bias): Applying human-like thinking patterns...");
        
        try {
            // Extract user ID and query
            String userId = extractUserId(request);
            String userQuery = extractUserMessage(request);
            
            if (userQuery.isEmpty()) {
                logger.info("⚠️ Brain 10: Empty query, proceeding");
                return chain.nextCall(request);
            }
            
            // Record interaction
            biasEngine.recordInteraction(userId, userQuery);
            
            // Get response from chain
            ChatClientResponse response = chain.nextCall(request);
            
            // Extract response text
            String responseText = response.chatResponse().getResult().getOutput().getText();
            
            // Apply cognitive biases
            String biasedResponse = biasEngine.applyBiases(userId, userQuery, responseText);
            
            // Log bias application
            logBiasApplication(userId);
            
            logger.info("✅ Brain 10: Cognitive biases applied - {}", biasEngine.getBiasSummary(userId));
            logger.debug("📝 Original length: {}, Biased length: {}", responseText.length(), biasedResponse.length());
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Brain 10: Error applying cognitive biases - {}", e.getMessage(), e);
            // Continue chain even if bias application fails
            return chain.nextCall(request);
        }
    }
    
    /**
     * Extract user ID from request
     */
    private String extractUserId(ChatClientRequest request) {
        try {
            if (request.prompt() != null && request.prompt().getInstructions() != null) {
                for (var message : request.prompt().getInstructions()) {
                    if (message instanceof UserMessage) {
                        UserMessage userMsg = (UserMessage) message;
                        String text = userMsg.getText();
                        if (text.contains("user_id:")) {
                            return text.substring(text.indexOf("user_id:") + 8).split(" ")[0];
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Could not extract user ID: {}", e.getMessage());
        }
        return "default_user";
    }
    
    /**
     * Extract user message from request
     */
    private String extractUserMessage(ChatClientRequest request) {
        try {
            if (request.prompt() != null && request.prompt().getInstructions() != null) {
                StringBuilder query = new StringBuilder();
                for (var message : request.prompt().getInstructions()) {
                    if (message instanceof UserMessage) {
                        UserMessage userMsg = (UserMessage) message;
                        query.append(userMsg.getText()).append(" ");
                    }
                }
                return query.toString().trim();
            }
        } catch (Exception e) {
            logger.debug("Could not extract user message: {}", e.getMessage());
        }
        return "";
    }
    
    /**
     * Log bias application details
     */
    private void logBiasApplication(String userId) {
        CognitiveBias activeBias = biasEngine.getActiveBias(userId);
        float biasStrength = biasEngine.getBiasStrength(userId);
        
        logger.info("🧠 Cognitive Bias Profile:");
        logger.info("   🎯 Active Bias: {}", activeBias.getDisplayName());
        logger.info("   💪 Bias Strength: {:.0f}%", biasStrength * 100);
        logger.info("   📝 Description: {}", activeBias.getDescription());
        
        if (activeBias.isPositive()) {
            logger.info("   ✅ Positive bias - enhancing helpful thinking");
        } else if (activeBias.isNegative()) {
            logger.info("   ⚠️ Negative bias - simulating human limitations");
        } else {
            logger.info("   ⚪ Neutral bias - balanced thinking");
        }
    }
}
