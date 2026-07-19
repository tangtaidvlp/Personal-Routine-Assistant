package com.tom.payment.routinemanager.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AiChatService {

    private final ChatClient chatClient;

    public AiChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("You are a helpful Personal Routine Assistant. Help the user manage their daily and default routines.")
                .defaultTools(
                        "addDefaultTasksFunction",
                        "updateDefaultTasksFunction",
                        "deleteDefaultTasksFunction",
                        "addDailyTasksFunction",
                        "updateDailyTasksFunction",
                        "deleteDailyTasksFunction"
                )
                .build();
    }

    public String chat(UUID userId, String userMessage) {
        return chatClient.prompt()
                .system("The current user's ID is: " + userId + ". You must use this ID whenever a tool function requires a 'userId' parameter.")
                .user(userMessage)
                .call()
                .content();
    }
}
