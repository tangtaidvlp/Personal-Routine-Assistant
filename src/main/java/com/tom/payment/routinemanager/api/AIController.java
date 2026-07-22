package com.tom.payment.routinemanager.api;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tom.payment.routinemanager.dto.ChatRequest;
import com.tom.payment.routinemanager.dto.ChatResponse;
import com.tom.payment.routinemanager.service.AiChatService;
import com.tom.payment.routinemanager.service.RoutineService;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private static final Logger logger = LoggerFactory.getLogger(AIController.class);

    private final RoutineService routineService;
    private final AiChatService aiChatService;

    public AIController(RoutineService routineService, AiChatService aiChatService) {
        this.routineService = routineService;
        this.aiChatService = aiChatService;
    }

    @PostMapping("/chat/{userId}")
    public ResponseEntity<ChatResponse> chatWithAi(
            @PathVariable UUID userId,
            @RequestBody ChatRequest request) {
        logger.info("AI chat request for user {}: {}", userId, request.getMessage());
        String aiReply = aiChatService.chat(userId, request.getMessage());
        ChatResponse response = new ChatResponse();
        response.setReply(aiReply);
        logger.info("AI chat response for user {}: {}", userId, aiReply);
        return ResponseEntity.ok(response);
    }

}
