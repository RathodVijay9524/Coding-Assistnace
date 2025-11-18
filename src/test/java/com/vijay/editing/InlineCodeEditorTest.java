package com.vijay.editing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class InlineCodeEditorTest {

    private OpenAiChatModel chatModel;
    private ChatClient chatClient;
    private InlineCodeEditor editor;

    @BeforeEach
    void setUp() {
        chatModel = Mockito.mock(OpenAiChatModel.class);
        chatClient = Mockito.mock(ChatClient.class, Mockito.RETURNS_DEEP_STUBS);
        // InlineCodeEditor builds its own ChatClient, so we mock the builder via the model.
        // For testing, we only care that calling applyInlineEdit returns a structured result,
        // so we stub the prompt chain on our own ChatClient and pass it via a subclass.
        editor = new InlineCodeEditor(chatModel) {
            @Override
            public CodeEditResult applyInlineEdit(CodeEditRequest request) {
                // Bypass actual ChatClient usage and directly call parseEditResult
                String suggestion = "public void m() { int a = 2; }";
                return parseForTest(request.getSelection().getCode(), suggestion, request.getInstruction());
            }

            private CodeEditResult parseForTest(String original, String suggested, String instruction) {
                return CodeEditResult.builder()
                        .originalCode(original)
                        .suggestedEdit(suggested)
                        .confidence(0.9)
                        .explanation("Applied: " + instruction)
                        .alternativeEdits(new java.util.ArrayList<>())
                        .editType("GENERAL_EDIT")
                        .linesChanged(1)
                        .breakingChange(false)
                        .build();
            }
        };
    }

    @Test
    @DisplayName("validateEdit should reject low confidence and breaking changes")
    void validateEdit_rules() {
        CodeEditResult lowConfidence = CodeEditResult.builder()
                .suggestedEdit("public void m() {}")
                .confidence(0.5)
                .breakingChange(false)
                .build();

        CodeEditResult breaking = CodeEditResult.builder()
                .suggestedEdit("void m() {}")
                .confidence(0.9)
                .breakingChange(true)
                .build();

        CodeEditResult ok = CodeEditResult.builder()
                .suggestedEdit("public void m() {}")
                .confidence(0.9)
                .breakingChange(false)
                .build();

        assertThat(editor.validateEdit(lowConfidence)).isFalse();
        assertThat(editor.validateEdit(breaking)).isFalse();
        assertThat(editor.validateEdit(ok)).isTrue();
    }
}
