package com.vijay.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CodeGenerationToolServiceTest {

    private ObjectProvider<ChatClient> chatClientProvider;
    private ChatClient chatClient;
    private CodeGenerationToolService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        chatClientProvider = Mockito.mock(ObjectProvider.class);
        chatClient = Mockito.mock(ChatClient.class, Mockito.RETURNS_DEEP_STUBS);
        objectMapper = new ObjectMapper();
        when(chatClientProvider.getObject()).thenReturn(chatClient);
        service = new CodeGenerationToolService(chatClientProvider, objectMapper);
    }

    @Test
    @DisplayName("generateCode should call ChatClient and return JSON with code and explanation")
    void generateCode_returnsJson() throws Exception {
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn("generated code snippet");

        String json = service.generateCode("Java", "Add two numbers", "Use clean code");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("language").asText()).isEqualTo("Java");
        assertThat(root.get("description").asText()).contains("Add two numbers");
        assertThat(root.get("code").asText()).contains("generated code snippet");
        assertThat(root.get("explanation").asText()).contains("Generated Java code");
        assertThat(root.get("status").asText()).isEqualTo("success");
    }

    @Test
    @DisplayName("generateBoilerplate should return JSON structure with project info")
    void generateBoilerplate_returnsJson() throws Exception {
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn("project structure");

        String json = service.generateBoilerplate("SpringBoot", "DemoProject", "REST, JPA");

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("projectType").asText()).isEqualTo("SpringBoot");
        assertThat(root.get("projectName").asText()).isEqualTo("DemoProject");
        assertThat(root.get("structure").asText()).contains("project structure");
        assertThat(root.get("status").asText()).isEqualTo("success");
    }
}
