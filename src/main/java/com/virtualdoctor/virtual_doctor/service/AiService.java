package com.virtualdoctor.virtual_doctor.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private final ChatClient doctorChatClient;
    private final ChatClient.Builder plainClientBuilder;

    public AiService(ChatClient doctorChatClient, ChatClient.Builder plainClientBuilder) {
        this.doctorChatClient = doctorChatClient;
        this.plainClientBuilder = plainClientBuilder;
    }

    public String chat(Long sessionId, String userMessage) {
        return doctorChatClient.prompt()
                .user(userMessage)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, String.valueOf(sessionId)))
                .call()
                .content();
    }

    // One-shot summary call - no tools, no memory needed, full transcript passed in directly.
    public String summarizeConversation(String fullTranscript, String userContext) {
        String systemPrompt = "You are a medical scribe AI. ..." // unchanged from your original
                + (userContext != null && !userContext.isEmpty() ? "\n\nPatient Profile:\n" + userContext : "");

        return plainClientBuilder.build()
                .prompt()
                .system(systemPrompt)
                .user("Conversation transcript:\n" + fullTranscript)
                .call()
                .content();
    }
}