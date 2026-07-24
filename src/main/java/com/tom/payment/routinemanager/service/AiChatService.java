package com.tom.payment.routinemanager.service;

import java.util.UUID;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.tom.payment.routinemanager.service.aitools.DailyRoutineAiTools;
import com.tom.payment.routinemanager.service.aitools.DefaultRoutineAiTools;

/**
 * Service that wires the Gemini ChatClient with our @Tool-annotated
 * tool beans and handles the chat interaction for each user.
 */
@Service
public class AiChatService {

    private final ChatClient chatClient;

    public AiChatService(
            ChatClient.Builder chatClientBuilder,
            DefaultRoutineAiTools defaultRoutineAiTools,
            DailyRoutineAiTools dailyRoutineAiTools) {
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        You are a helpful Personal Routine Assistant.
                        Help the user manage their daily and default routines.
                        When the user asks to add, update, or delete tasks, use the available tools.
                        Always confirm what you did after making changes.
                        """)
                .defaultTools(defaultRoutineAiTools, dailyRoutineAiTools)
                .build();
    }

    public String chat(UUID userId, String userMessage) {
        return chatClient.prompt()
                .system("The current user's ID is: " + userId
                        + ". You MUST use this ID whenever a tool requires a 'userId' parameter. "
                        + "Today's date is: " + java.time.LocalDate.now() + ".")
                .user(userMessage)
                .call()
                .content();
    }

    // For later usage
    public String chatEnhance(UUID userId, UUID defaultRoutineId, String userMessage) {
        return chatClient.prompt()
                .system("The current user's ID is: " + userId
                        + ". You MUST use this ID whenever a tool requires a 'userId' parameter. "
                        + "The current default routine's ID is: " + defaultRoutineId + ". Ignore if not relevant or available. "
                        + "Today's date is: " + java.time.LocalDate.now() + ".")
                .user(userMessage)
                .call()
                .content();
    }
}
