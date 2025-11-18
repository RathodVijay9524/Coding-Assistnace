package com.vijay.manager;

import com.vijay.dto.AgentPlan;
import com.vijay.service.*;
import com.vijay.util.AgentPlanHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SelfRefineV3AdvisorTest {

    private OpenAiChatModel chatModel;
    private SupervisorBrain supervisorBrain;
    private TokenCountingService tokenCountingService;
    private ConsistencyCheckService consistencyCheckService;
    private HallucinationDetector hallucinationDetector;
    private OutputMerger outputMerger;
    private SelfRefineV3Advisor advisor;

    @BeforeEach
    void setUp() {
        chatModel = Mockito.mock(OpenAiChatModel.class);
        supervisorBrain = Mockito.mock(SupervisorBrain.class);
        tokenCountingService = Mockito.mock(TokenCountingService.class);
        consistencyCheckService = Mockito.mock(ConsistencyCheckService.class);
        hallucinationDetector = Mockito.mock(HallucinationDetector.class);
        outputMerger = Mockito.mock(OutputMerger.class);

        advisor = new SelfRefineV3Advisor(
                chatModel,
                supervisorBrain,
                tokenCountingService,
                consistencyCheckService,
                hallucinationDetector,
                outputMerger
        );
    }

    @AfterEach
    void tearDown() {
        AgentPlanHolder.clear();
    }

    @Test
    @DisplayName("adviseCall should bypass heavy evaluation for simple queries (complexity <= 3)")
    void adviseCall_simpleQuery_skipsEvaluation() {
        AgentPlan plan = new AgentPlan()
                .setIntent("GENERAL")
                .setComplexity(2)
                .setStrategy("FAST_RECALL");
        AgentPlanHolder.setPlan(plan);

        ChatClientRequest request = mock(ChatClientRequest.class);
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse response = mock(ChatClientResponse.class, Mockito.RETURNS_DEEP_STUBS);
        org.springframework.ai.chat.prompt.Prompt prompt = mock(org.springframework.ai.chat.prompt.Prompt.class);
        UserMessage userMessage = mock(UserMessage.class);

        when(request.prompt()).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of(userMessage));
        when(userMessage.getText()).thenReturn("Hello, how are you?");
        when(chain.nextCall(request)).thenReturn(response);

        ChatClientResponse result = advisor.adviseCall(request, chain);

        assertThat(result).isSameAs(response);
        // For simple queries, evaluation is skipped and no heavy services should be invoked
        verifyNoInteractions(tokenCountingService, consistencyCheckService, hallucinationDetector, outputMerger);
        verify(chain, times(1)).nextCall(request);
    }
}
