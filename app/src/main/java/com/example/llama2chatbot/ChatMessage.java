package com.example.llama2chatbot;

public class ChatMessage {
    public String message;
    public boolean isUser;

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }
}
