package com.vijay.manager;

import com.vijay.context.GlobalBrainContext;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return 10;  // Run AFTER Brain 0 (Conductor), BEFORE Brain 2 (ToolCall)
    }

    // ===== IAgentBrain Implementation =====
    @Override
    public String getBrainName() {
        return "dynamicContextAdvisor";
    }

    @Override
    public String getBrainDescription() {
        return "Reads the plan from Brain 0 (Conductor), applies brain selection strategy based on complexity, enforces tool approval";
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("🧠 Brain 1 (Dynamic Context): Reading master plan and applying execution strategy...");

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
                return handleFallbackMode(request, chain);
            }

            // STEP 2: Extract user query
            String userQuery = extractUserMessage(request);
            logger.info("   📝 Query: {}", userQuery.length() > 60 ? userQuery.substring(0, 60) + "..." : userQuery);

            // ✅ STEP 3: RESPECT CONDUCTOR'S BRAIN SELECTION
            List<String> specialistBrains = selectBrainsBasedOnPlan(masterPlan, userQuery);
            logger.info("   🧠 Specialist brains to activate: {} - {}", specialistBrains.size(), specialistBrains);

            // ✅ STEP 4: ONLY USE CONDUCTOR-APPROVED TOOLS
            List<String> approvedTools = masterPlan.getRequiredTools();
            logger.info("   🔧 Tools to use: {} - {}", approvedTools.size(), approvedTools);

            // ✅ STEP 5: Store plan context for downstream advisors
            storePlanContext(masterPlan, specialistBrains, approvedTools);

            // STEP 6: Build context injection for specialist brains
            String contextInjection = buildContextInjection(specialistBrains, approvedTools, masterPlan);
            logger.info("   ✅ Context prepared for {} specialist brains and {} tools",
                    specialistBrains.size(), approvedTools.size());

            // STEP 7: Continue to next advisor in chain
            ChatClientResponse response = chain.nextCall(request);

            logger.info("✅ Brain 1: Dynamic context injection complete");
            return response;

        } catch (Exception e) {
            logger.error("❌ Brain 1: Error in dynamic context processing - {}", e.getMessage(), e);
            // Continue chain even if context processing fails
            return chain.nextCall(request);
        }
    }

    /**
     * ✅ KEY FIX: Select brains based on Conductor's plan
     * If Conductor specified brains → use them
     * If Conductor said "no specialist brains" (empty list) → use core brains only based on complexity
     * Otherwise → fallback to BrainFinder discovery
     */
    private List<String> selectBrainsBasedOnPlan(AgentPlan plan, String userQuery) {
        // Case 1: Conductor explicitly specified which brains to use
        if (!plan.getSelectedBrains().isEmpty()) {
            logger.info("   ✅ Using CONDUCTOR-SPECIFIED brains");
            return new ArrayList<>(plan.getSelectedBrains());
        }

        // Case 2: Conductor said "no specialist brains needed" (empty list in plan)
        // Use CORE ONLY brains based on complexity
        logger.info("   ✅ Conductor specified NO specialist brains - using CORE ONLY strategy");
        return selectCoreBrains(plan.getComplexity());
    }

    /**
     * ✅ Core brain selection strategy based on complexity
     * Complexity 1-2: Minimal (Conductor + ToolCall + Personality)
     * Complexity 3-5: Medium (+ Safety + Memory)
     * Complexity 6+: Full (+ All specialists)
     */
    private List<String> selectCoreBrains(int complexity) {
        List<String> coreBrains = new ArrayList<>();

        // Always include these
        coreBrains.add("conductorAdvisor");
        coreBrains.add("toolCallAdvisor");

        if (complexity <= 2) {
            // Simple query: just core + personality
            coreBrains.add("personalityAdvisor");
            logger.debug("   📊 Complexity {}: Using MINIMAL brain set (3 brains)", complexity);
        } else if (complexity <= 5) {
            // Medium query: add safety
            coreBrains.add("personalityAdvisor");
            coreBrains.add("safetyGuardrailAdvisor");
            logger.debug("   📊 Complexity {}: Using MEDIUM brain set (4 brains)", complexity);
        } else {
            // Complex query: add full support
            coreBrains.add("personalityAdvisor");
            coreBrains.add("safetyGuardrailAdvisor");
            coreBrains.add("conversationMemoryAdvisor");
            coreBrains.add("selfRefineV3Advisor");
            logger.debug("   📊 Complexity {}: Using FULL brain set (6 brains)", complexity);
        }

        return coreBrains;
    }

    /**
     * ✅ Store plan context in GlobalBrainContext for downstream advisors
     */
    private void storePlanContext(AgentPlan plan, List<String> selectedBrains, List<String> approvedTools) {
        try {
            // Store in GlobalBrainContext so other advisors can access
            Map<String, Object> planContext = new HashMap<>();
            planContext.put("masterPlan", plan);
            planContext.put("selectedBrains", selectedBrains);
            planContext.put("approvedTools", approvedTools);
            planContext.put("complexity", plan.getComplexity());
            planContext.put("intent", plan.getIntent());

            // Store for ToolCallAdvisor to read
            GlobalBrainContext.put("planContext", planContext);

        } catch (Exception e) {
            logger.warn("⚠️ Failed to store plan context: {}", e.getMessage());
        }
    }

    /**
     * Fallback mode when no master plan exists
     */
    private ChatClientResponse handleFallbackMode(ChatClientRequest request, CallAdvisorChain chain) {
        logger.info("   🔄 Fallback mode: Discovering brains and tools dynamically");

        String userQuery = extractUserMessage(request);

        // Use BrainFinder and ToolFinder as fallback
        List<String> specialistBrains = brainFinderService.findBrainsFor(userQuery);
        List<String> requiredTools = toolFinderService.findToolsFor(userQuery);

        logger.info("   🧠 Fallback brains: {} - {}", specialistBrains.size(), specialistBrains);
        logger.info("   🔧 Fallback tools: {} - {}", requiredTools.size(), requiredTools);

        buildContextInjection(specialistBrains, requiredTools, null);

        return chain.nextCall(request);
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
     * ✅ Now includes plan information
     */
    private String buildContextInjection(List<String> specialistBrains, List<String> requiredTools, AgentPlan plan) {
        StringBuilder context = new StringBuilder();

        context.append("\n[EXECUTION CONTEXT]\n");

        // Include plan metadata if available
        if (plan != null) {
            context.append("Plan Intent: ").append(plan.getIntent()).append("\n");
            context.append("Plan Complexity: ").append(plan.getComplexity()).append("/10\n");
            context.append("Plan Strategy: ").append(plan.getStrategy()).append("\n");
        }

        if (!specialistBrains.isEmpty()) {
            context.append("Active Brains: ");
            context.append(String.join(", ", specialistBrains));
            context.append("\n");
        }

        if (!requiredTools.isEmpty()) {
            context.append("Approved Tools: ");
            context.append(String.join(", ", requiredTools));
            context.append("\n");
        } else {
            context.append("Approved Tools: NONE (Answer from knowledge only)\n");
        }

        context.append("[END EXECUTION CONTEXT]\n");

        return context.toString();
    }
}
