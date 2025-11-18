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

class NLToCodeToolServiceTest {

    private ObjectMapper objectMapper;
    private ObjectProvider<ChatClient> chatClientProvider;
    private ChatClient chatClient;
    private NLToCodeToolService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        objectMapper = new ObjectMapper();
        chatClientProvider = mock(ObjectProvider.class);
        // Deep-stub ChatClient so prompt().user(...).call().content() works without explicit types
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);

        org.mockito.Mockito.when(chatClientProvider.getObject()).thenReturn(chatClient);
        org.mockito.Mockito.when(chatClient.prompt().user(anyString()).call().content()).thenReturn("// code");

        service = new NLToCodeToolService(chatClientProvider, objectMapper);
    }

    @Test
    @DisplayName("nlToCode should wrap generated code and metadata into JSON")
    void nlToCode_basic() throws Exception {
        String json = service.nlToCode("reverse a list", "Java", "function");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("code").asText()).contains("code");
        assertThat(root.get("language").asText()).isEqualTo("Java");
        assertThat(root.get("codeType").asText()).isEqualTo("function");
        assertThat(root.get("description").asText()).contains("reverse a list");
    }

    @Test
    @DisplayName("generateFunction should wrap returned function and metadata into JSON")
    void generateFunction_basic() throws Exception {
        String json = service.generateFunction("sum two numbers", "Java", "{a:int,b:int}");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("function").asText()).contains("code");
        assertThat(root.get("language").asText()).isEqualTo("Java");
    }

    @Test
    @DisplayName("generateClass should wrap returned class and metadata into JSON")
    void generateClass_basic() throws Exception {
        String json = service.generateClass("DTO for user", "Java", "{name,email}");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("class").asText()).contains("code");
        assertThat(root.get("language").asText()).isEqualTo("Java");
    }

    @Test
    @DisplayName("generateAlgorithm should wrap returned algorithm and metadata into JSON")
    void generateAlgorithm_basic() throws Exception {
        String json = service.generateAlgorithm("binary search", "Java", "O(log n)");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("algorithm").asText()).contains("code");
        assertThat(root.get("language").asText()).isEqualTo("Java");
    }

    @Test
    @DisplayName("generateApiEndpoint should wrap returned endpoint and metadata into JSON")
    void generateApiEndpoint_basic() throws Exception {
        String json = service.generateApiEndpoint("CRUD for users", "Spring", "GET");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("endpoint").asText()).contains("code");
        assertThat(root.get("framework").asText()).isEqualTo("Spring");
        assertThat(root.get("httpMethod").asText()).isEqualTo("GET");
    }
}
