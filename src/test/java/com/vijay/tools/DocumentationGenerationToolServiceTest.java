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

class DocumentationGenerationToolServiceTest {

    private ObjectMapper objectMapper;
    private ObjectProvider<ChatClient> chatClientProvider;
    private ChatClient chatClient;
    private DocumentationGenerationToolService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        objectMapper = new ObjectMapper();
        chatClientProvider = mock(ObjectProvider.class);
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);

        org.mockito.Mockito.when(chatClientProvider.getObject()).thenReturn(chatClient);
        org.mockito.Mockito.when(chatClient.prompt().user(anyString()).call().content())
                .thenReturn("doc-content");

        service = new DocumentationGenerationToolService(chatClientProvider, objectMapper);
    }

    @Test
    @DisplayName("generateDocumentation with type=all should include all doc sections and summary count 4")
    void generateDocumentation_all_returnsAllSections() throws Exception {
        String code = "public class Sample { }";

        String json = service.generateDocumentation(code, "all", "java");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("apiDocumentation").asText()).contains("doc-content");
        assertThat(root.get("readme").asText()).contains("doc-content");
        assertThat(root.get("userGuide").asText()).contains("doc-content");
        assertThat(root.get("codeComments").asText()).contains("doc-content");
        assertThat(root.get("architectureOverview").asText()).contains("doc-content");
        assertThat(root.get("usageExamples").asText()).contains("doc-content");

        String summary = root.get("summary").asText();
        assertThat(summary).contains("Generated 4 documentation artifacts");
    }

    @Test
    @DisplayName("generateDocumentation with type=api should only populate API docs plus shared sections")
    void generateDocumentation_apiOnly_populatesApiAndSharedSections() throws Exception {
        String code = "public class Sample { }";

        String json = service.generateDocumentation(code, "api", "java");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("apiDocumentation").asText()).contains("doc-content");
        // optional sections should be absent
        assertThat(root.has("readme")).isFalse();
        assertThat(root.has("userGuide")).isFalse();
        assertThat(root.has("codeComments")).isFalse();

        // shared sections always present
        assertThat(root.get("architectureOverview").asText()).contains("doc-content");
        assertThat(root.get("usageExamples").asText()).contains("doc-content");

        String summary = root.get("summary").asText();
        assertThat(summary).contains("Generated 1 documentation artifacts");
    }

    @Test
    @DisplayName("generateDocumentation should still return fallback strings when ChatClient throws")
    @SuppressWarnings("unchecked")
    void generateDocumentation_handlesChatClientError() throws Exception {
        ObjectProvider<ChatClient> failingProvider = mock(ObjectProvider.class);
        ChatClient failingClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        org.mockito.Mockito.when(failingProvider.getObject()).thenReturn(failingClient);
        org.mockito.Mockito.when(failingClient.prompt().user(anyString()).call().content())
                .thenThrow(new RuntimeException("boom"));

        DocumentationGenerationToolService failingService =
                new DocumentationGenerationToolService(failingProvider, objectMapper);

        String json = failingService.generateDocumentation("code", "api", "java");

        JsonNode root = objectMapper.readTree(json);
        String apiDoc = root.get("apiDocumentation").asText();
        assertThat(apiDoc).startsWith("# API Documentation");
        assertThat(apiDoc).contains("Failed to generate API documentation");
    }
}
