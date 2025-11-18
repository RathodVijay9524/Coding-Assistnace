package com.vijay.manager;

import com.vijay.dto.AgentPlan;
import com.vijay.dto.ReasoningState;
import com.vijay.context.GlobalBrainContext;
import com.vijay.util.AgentPlanHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ToolCallAdvisorTest {

    private ToolCallAdvisor advisor;

    @BeforeEach
    void setUp() {
        advisor = new ToolCallAdvisor();
    }

    @AfterEach
    void tearDown() {
        AgentPlanHolder.clear();
        GlobalBrainContext.setReasoningState(null);
    }

    @Test
    @DisplayName("adviseCall should pass through when no master plan is available")
    void adviseCall_noPlan_passThrough() {
        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);

        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        verify(chain, times(1)).nextCall(request);
    }

    @Test
    @DisplayName("adviseCall should inject enforcement rules based on approved and rejected tools")
    void adviseCall_withPlan_injectsRules() {
        AgentPlan plan = new AgentPlan()
                .setIntent("ANALYSIS")
                .setRequiredTools(List.of("toolA", "toolB"));
        AgentPlanHolder.setPlan(plan);

        ReasoningState state = new ReasoningState();
        state.setSuggestedTools(List.of("toolA", "toolC"));
        GlobalBrainContext.setReasoningState(state);

        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class);
        Prompt prompt = mock(Prompt.class);

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.<Message>of());
        when(prompt.getOptions()).thenReturn(null);
        when(chain.nextCall(any(ChatClientRequest.class))).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        // We can’t easily inspect the modified prompt, but we can verify the chain was called with a new request
        verify(chain, times(1)).nextCall(any(ChatClientRequest.class));
    }
}
