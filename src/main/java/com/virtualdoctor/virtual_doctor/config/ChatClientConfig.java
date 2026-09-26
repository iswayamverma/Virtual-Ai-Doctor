package com.virtualdoctor.virtual_doctor.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    private static final String SYSTEM_PROMPT = """
        You are an AI-powered virtual doctor assistant for Indian patients.
        ... (your existing multilingual + doctor-behavior instructions go here, minus the
        [SEVERITY: ...] tag instructions - severity is now handled by calling the
        flagHighSeverity tool when appropriate, not by appending a tag to your reply.)
        """;

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(50)
                .build();
    }

    // Uses the in-process 'consultationTools' bean from ToolConfig.java directly -
    // no network loopback, no startup ordering issue.
    @Bean
    public ChatClient doctorChatClient(ChatClient.Builder builder,
                                        ToolCallbackProvider consultationTools,
                                        ChatMemory chatMemory) {
        return builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultToolCallbacks(consultationTools)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}