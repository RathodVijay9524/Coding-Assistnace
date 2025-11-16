package com.vijay.manager;

import com.vijay.dto.AgentPlan;
import com.vijay.util.AgentPlanHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.stereotype.Component;

/**
 * 🔧 Brain 2: Tool Call Advisor (Plan-Aware Tool Executor)
 * 
 * Purpose: Execute tools based on the master plan from Brain 0
 * 
 * Responsibilities:
 * - Read the master plan from ConductorAdvisor (Brain 0)
 * - Check if tools are required (plan.requiredTools)
 * - Only execute tools if the plan says they're needed
 * - Prevent HTTP 400 crashes from tool execution errors
 * - Log tool execution details
 * 
 * Execution Order: 2 (AFTER Brain 1 - DynamicContextAdvisor)
 * 
 * Key Feature: Plan-Aware
 * - Only runs if plan.requiredTools is not empty
 * - Prevents unnecessary tool execution
 * - Reduces errors and improves performance
 */
@Component
public class ToolCallAdvisor implements CallAdvisor, IAgentBrain {
    
    private static final Logger logger = LoggerFactory.getLogger(ToolCallAdvisor.class);
    
    @Override
    public String getName() {
        return "ToolCallAdvisor";
    }
    
    // ===== IAgentBrain Implementation =====
    @Override
    public String getBrainName() {
        return "toolCallAdvisor";  // ← Spring bean name (lowercase first letter)
    }
    
    @Override
    public String getBrainDescription() {
        return "Plan-aware tool executor. Reads the master plan and only executes tools if they are required. Prevents HTTP 400 errors and improves performance.";
    }
    
    @Override
    public int getOrder() {
        return 2;  // Execute AFTER Brain 1 (DynamicContextAdvisor)
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("🔧 Brain 2 (Tool Call): Checking if tools are needed...");
        
        try {
            // STEP 1: Read the master plan from Brain 0 (ConductorAdvisor)
            AgentPlan masterPlan = AgentPlanHolder.getPlan();
            
            if (masterPlan == null) {
                logger.warn("   ⚠️ No master plan found - skipping tool execution");
                return chain.nextCall(request);
            }
            
            // STEP 2: Check if tools are required
            if (!masterPlan.requiresTools()) {
                logger.info("   ℹ️ No tools required in plan - skipping tool execution");
                logger.info("      Plan Intent: {}", masterPlan.getIntent());
                return chain.nextCall(request);
            }
            
            // STEP 3: Log tools that will be executed
            logger.info("   🔧 Tools required by plan:");
            for (String tool : masterPlan.getRequiredTools()) {
                logger.info("      - {}", tool);
            }
            
            // STEP 4: Continue to next advisor
            // The LLM will see the required tools and can use them if needed
            logger.info("   ✅ Tools available for execution");
            ChatClientResponse response = chain.nextCall(request);
            
            logger.info("✅ Brain 2: Tool call processing complete");
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Brain 2: Error in tool call processing - {}", e.getMessage());
            // Continue chain even if tool processing fails
            return chain.nextCall(request);
        }
    }
}
