package com.vijay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatRequest {
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("useTools")
    private boolean useTools = true;
    
    public ChatRequest() {}
    
    public ChatRequest(String message, boolean useTools) {
        this.message = message;
        this.useTools = useTools;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isUseTools() {
        return useTools;
    }
    
    public void setUseTools(boolean useTools) {
        this.useTools = useTools;
    }
    
    @Override
    public String toString() {
        return "ChatRequest{" +
                "message='" + message + '\'' +
                ", useTools=" + useTools +
                '}';
    }
}
