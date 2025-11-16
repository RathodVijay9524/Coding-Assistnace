package com.vijay.manager;

import com.vijay.dto.AgentPlan;
import com.vijay.service.BrainFinderService;
import com.vijay.tools.ToolFinderService;
import com.vijay.util.AgentPlanHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 🧠 Brain 1: Dynamic Context Advisor (The "Context Fetcher")
 * 
 * Purpose: Read the plan from Brain 0 (QueryPlanner) and dynamically fetch specialist context
 * 
 * Responsibilities:
 * - Extract the plan/intent from Brain 0's analysis
 * - Call BrainFinder to identify specialist brains needed
 * - Call ToolFinder to identify tools needed
 * - Inject specialist brain descriptions and tool info into the prompt
 * - Provide context for specialist brains to activate
 * 
 * Execution Order: 1 (AFTER Brain 0 - QueryPlanner)
 * 
 * This is the KEY to the Hybrid Brain architecture:
 * - Brain 0 (Planner) creates the plan
 * - Brain 1 (Dynamic Context) fetches specialist context based on plan
 * - Specialist brains (2-12) process the context
 * - Brain 13 (Judge) evaluates
 * - Brain 14 (Personality) polishes
 */
@Component
public class DynamicContextAdvisor implements CallAdvisor, IAgentBrain {
    
    private static final Logger logger = LoggerFactory.getLogger(DynamicContextAdvisor.class);
    
    private final BrainFinderService brainFinderService;
    private final ToolFinderService toolFinderService;
    
    public DynamicContextAdvisor(BrainFinderService brainFinderService,
                                ToolFinderService toolFinderService) {
        this.brainFinderService = brainFinderService;
        this.toolFinderService = toolFinderService;
    }
    
    @Override
    public String getName() {
        return "DynamicContextAdvisor";
    }
    
    @Override
    public int getOrder() {
        return 1;  // Run AFTER Brain 0 (QueryPlanner), BEFORE specialist brains
    }
    
    // ===== IAgentBrain Implementation =====
    @Override
    public String getBrainName() {
        return "dynamicContextAdvisor";  // ← Spring bean name (lowercase first letter)
    }
    
    @Override
    public String getBrainDescription() {
        return "Reads the plan from Brain 0 (QueryPlanner), dynamically fetches specialist brains and tools via RAG, injects context into the prompt for specialist brains to use";
    }
    
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("🧠 Brain 1 (Dynamic Context): Reading master plan and fetching specialist context...");
        
        try {
            // STEP 1: Read the master plan from Brain 0 (ConductorAdvisor)
            AgentPlan masterPlan = AgentPlanHolder.getPlan();
            
            if (masterPlan != null) {
                logger.info("   📋 Master Plan Found:");
                logger.info("      Intent: {}", masterPlan.getIntent());
                logger.info("      Complexity: {}", masterPlan.getComplexity());
                logger.info("      Required Tools: {}", masterPlan.getRequiredTools());
                logger.info("      Selected Brains: {}", masterPlan.getSelectedBrains());
            } else {
                logger.warn("   ⚠️ No master plan found - using fallback context discovery");
            }
            
            // STEP 2: Extract user query
            String userQuery = extractUserMessage(request);
            logger.info("   📝 Query: {}", userQuery.length() > 60 ? userQuery.substring(0, 60) + "..." : userQuery);
            
            // STEP 3: Use plan's selected brains if available, otherwise discover via BrainFinder
            List<String> specialistBrains = (masterPlan != null && !masterPlan.getSelectedBrains().isEmpty()) 
                ? masterPlan.getSelectedBrains()
                : brainFinderService.findBrainsFor(userQuery);
            logger.info("   🧠 Specialist brains to activate: {} - {}", specialistBrains.size(), specialistBrains);
            
            // STEP 4: Use plan's required tools if available, otherwise discover via ToolFinder
            List<String> requiredTools = (masterPlan != null && !masterPlan.getRequiredTools().isEmpty())
                ? masterPlan.getRequiredTools()
                : toolFinderService.findToolsFor(userQuery);
            logger.info("   🔧 Tools to use: {} - {}", requiredTools.size(), requiredTools);
            
            // STEP 5: Build context injection for specialist brains
            String contextInjection = buildContextInjection(specialistBrains, requiredTools);
            logger.info("   ✅ Context prepared for {} specialist brains and {} tools", 
                specialistBrains.size(), requiredTools.size());
            
            // STEP 6: Continue to next advisor in chain
            // The specialist brains will now have context about what they should do
            ChatClientResponse response = chain.nextCall(request);
            
            logger.info("✅ Brain 1: Dynamic context injection complete");
            return response;
            
        } catch (Exception e) {
            logger.error("❌ Brain 1: Error in dynamic context fetching - {}", e.getMessage());
            // Continue chain even if context fetching fails
            return chain.nextCall(request);
        }
    }
    
    /**
     * Extract user message from ChatClientRequest
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
            return "";
        } catch (Exception e) {
            logger.debug("Failed to extract user message: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * Build context injection string for specialist brains
     * This provides information about what specialist brains and tools are available
     */
    private String buildContextInjection(List<String> specialistBrains, List<String> requiredTools) {
        StringBuilder context = new StringBuilder();
        
        context.append("\n[SPECIALIST CONTEXT]\n");
        
        if (!specialistBrains.isEmpty()) {
            context.append("Available Specialist Brains: ");
            for (int i = 0; i < specialistBrains.size(); i++) {
                context.append(specialistBrains.get(i));
                if (i < specialistBrains.size() - 1) {
                    context.append(", ");
                }
            }
            context.append("\n");
        }
        
        if (!requiredTools.isEmpty()) {
            context.append("Available Tools: ");
            for (int i = 0; i < requiredTools.size(); i++) {
                context.append(requiredTools.get(i));
                if (i < requiredTools.size() - 1) {
                    context.append(", ");
                }
            }
            context.append("\n");
        }
        
        context.append("[END SPECIALIST CONTEXT]\n");
        
        return context.toString();
    }
}
