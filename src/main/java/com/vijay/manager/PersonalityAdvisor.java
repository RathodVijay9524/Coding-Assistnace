package com.vijay.manager;

import com.vijay.service.PersonalityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.stereotype.Component;

/**
 * 🧠 Brain 9: Personality Advisor
 * 
 * Purpose: Apply consistent personality and communication style to responses
 * 
 * Responsibilities:
 * - Apply personality traits to responses
 * - Maintain consistent communication style
 * - Express values and principles
 * - Ensure personality consistency across interactions
 * - Adapt personality based on context
 * 
 * Execution Order: 800 (Late, after response generation but before final evaluation)
 */
@Component
public class PersonalityAdvisor implements CallAdvisor {
    
    private static final Logger logger = LoggerFactory.getLogger(PersonalityAdvisor.class);
    
    private final PersonalityEngine personalityEngine;
    
    public PersonalityAdvisor(PersonalityEngine personalityEngine) {
        this.personalityEngine = personalityEngine;
    }
    
    @Override
    public String getName() {
        return "PersonalityAdvisor";
    }
    
    @Override
    public int getOrder() {
        return 800; // Execute late, after response generation
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("🧠 Brain 9 (Personality): Applying personality to response...");
        
        try {
            // Get response from chain
            ChatClientResponse response = chain.nextCall(request);
            
            // Extract response text
            String responseText = response.chatResponse().getResult().getOutput().getText();
            
            // Apply personality
            String personalizedResponse = personalityEngine.applyPersonality(responseText);
            
            // Log personality application
            logPersonalityApplication();
            
            logger.info("✅ Brain 9: Personality applied - Archetype: {}", 
                personalityEngine.getTraits().getArchetype());
            logger.debug("📝 Original length: {}, Personalized length: {}", 
                responseText.length(), personalizedResponse.length());
            
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Brain 9: Error applying personality - {}", e.getMessage(), e);
            // Continue chain even if personality application fails
            return chain.nextCall(request);
        }
    }
    
    /**
     * Log personality application details
     */
    private void logPersonalityApplication() {
        logger.info("🎭 Personality Profile:");
        logger.info("   {}", personalityEngine.getPersonalitySummary());
        logger.info("   {}", personalityEngine.getStyleSummary());
        
        if (personalityEngine.isEmpathetic()) {
            logger.info("   ❤️ Empathetic responses enabled");
        }
        
        if (personalityEngine.isPatient()) {
            logger.info("   😌 Patient with user questions");
        }
        
        if (personalityEngine.isHelpful()) {
            logger.info("   🤝 Highly helpful and supportive");
        }
    }
}
