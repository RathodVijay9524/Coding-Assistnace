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

class GenerateFromDescriptionToolServiceTest {

    private ObjectMapper objectMapper;
    private ObjectProvider<ChatClient> chatClientProvider;
    private ChatClient chatClient;
    private GenerateFromDescriptionToolService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        objectMapper = new ObjectMapper();
        chatClientProvider = mock(ObjectProvider.class);
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);

        org.mockito.Mockito.when(chatClientProvider.getObject()).thenReturn(chatClient);
        org.mockito.Mockito.when(chatClient.prompt().user(anyString()).call().content()).thenReturn("project-code");

        service = new GenerateFromDescriptionToolService(chatClientProvider, objectMapper);
    }

    @Test
    @DisplayName("generateProject should wrap project and metadata into JSON")
    void generateProject_basic() throws Exception {
        String json = service.generateProject("demo project", "web", "Spring Boot");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("project").asText()).contains("project-code");
        assertThat(root.get("projectType").asText()).isEqualTo("web");
        assertThat(root.get("techStack").asText()).contains("Spring Boot");
    }
}
