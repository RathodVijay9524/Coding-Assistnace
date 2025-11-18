package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class QueryOptimizationToolServiceTest {

    private ObjectMapper objectMapper;
    private ObjectProvider<ChatClient> chatClientProvider;
    private ChatClient chatClient;
    private QueryOptimizationToolService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        objectMapper = new ObjectMapper();
        chatClientProvider = mock(ObjectProvider.class);
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);

        org.mockito.Mockito.when(chatClientProvider.getObject()).thenReturn(chatClient);
        org.mockito.Mockito.when(chatClient.prompt().user(anyString()).call().content()).thenReturn("analysis");

        service = new QueryOptimizationToolService(chatClientProvider, objectMapper);
    }

    @Test
    @DisplayName("optimizeQuery should include analysis, suggestions, optimizedQuery, and performanceComparison")
    void optimizeQuery_basic() throws Exception {
        String sql = "SELECT * FROM users";

        String json = service.optimizeQuery(sql, "MySQL", "all");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("analysis")).isTrue();
        assertThat(root.has("suggestions")).isTrue();
        assertThat(root.has("optimizedQuery")).isTrue();
        assertThat(root.has("performanceComparison")).isTrue();
    }
}
