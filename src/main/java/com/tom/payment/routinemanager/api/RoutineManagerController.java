package com.tom.payment.routinemanager.api;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tom.payment.routinemanager.dto.ChatRequest;
import com.tom.payment.routinemanager.dto.ChatResponse;
import com.tom.payment.routinemanager.model.DailyRoutine;
import com.tom.payment.routinemanager.model.DailyTask;
import com.tom.payment.routinemanager.model.DefaultRoutine;
import com.tom.payment.routinemanager.model.RoutineTaskTemplate;
import com.tom.payment.routinemanager.service.AiChatService;
import com.tom.payment.routinemanager.service.DailyRoutineService;
import com.tom.payment.routinemanager.service.DefaultRoutineService;

@RestController
@RequestMapping("/api/routine-manager")
public class RoutineManagerController {

    private final DefaultRoutineService routineService;
    private final DailyRoutineService dailyRoutineService;
    private final AiChatService aiChatService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public RoutineManagerController(DefaultRoutineService routineService,
                                    DailyRoutineService dailyRoutineService,
                                    AiChatService aiChatService) {
        this.routineService = routineService;
        this.dailyRoutineService = dailyRoutineService;
        this.aiChatService = aiChatService;
    }

    @PostMapping("/chat/{userId}")
    public ResponseEntity<ChatResponse> chatWithAi(
            @PathVariable UUID userId,
            @RequestBody ChatRequest request) {
        String aiReply = aiChatService.chat(userId, request.getMessage());
        ChatResponse response = new ChatResponse();
        response.setReply(aiReply);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/default-routine/{routineId}/tasks")
    public ResponseEntity<List<RoutineTaskTemplate>> addDefaultTasks(
            @PathVariable UUID routineId,
            @RequestBody String body) {
        JsonNode root;
        try {
            root = objectMapper.readTree(body);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Request body must be valid JSON", ex);
        }

        List<RoutineTaskTemplate> tasks;
        if (root.isArray()) {
            tasks = StreamSupport.stream(root.spliterator(), false)
                    .map(node -> objectMapper.convertValue(node, RoutineTaskTemplate.class))
                    .toList();
        } else if (root.isObject()) {
            RoutineTaskTemplate task = objectMapper.convertValue(root, RoutineTaskTemplate.class);
            tasks = List.of(task);
        } else {
            throw new IllegalArgumentException("Request body must be a JSON object or array of objects");
        }

        List<RoutineTaskTemplate> createdTasks = routineService.addDefaultTasks(routineId, tasks);
        return ResponseEntity.ok(createdTasks);
    }

    @PutMapping("/default-routine/tasks")
    public ResponseEntity<List<RoutineTaskTemplate>> updateDefaultTasks(
            @RequestBody List<RoutineTaskTemplate> tasksDetails) {
        List<RoutineTaskTemplate> updatedTasks = routineService.updateDefaultTasks(tasksDetails);
        return ResponseEntity.ok(updatedTasks);
    }

    @DeleteMapping("/default-routine/tasks")
    public ResponseEntity<Void> deleteDefaultTasks(@RequestParam List<UUID> taskIds) {
        routineService.deleteDefaultTasks(taskIds);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/daily-routine/{routineId}/tasks")
    public ResponseEntity<List<DailyTask>> addDailyTasks(
            @PathVariable UUID routineId,
            @RequestBody List<DailyTask> tasks) {
        List<DailyTask> createdTasks = dailyRoutineService.addDailyTasks(routineId, tasks);
        return ResponseEntity.ok(createdTasks);
    }

    @PutMapping("/daily-routine/tasks")
    public ResponseEntity<List<DailyTask>> updateDailyTasks(
            @RequestBody List<DailyTask> tasksDetails) {
        List<DailyTask> updatedTasks = dailyRoutineService.updateDailyTasks(tasksDetails);
        return ResponseEntity.ok(updatedTasks);
    }

    @DeleteMapping("/daily-routine/tasks")
    public ResponseEntity<Void> deleteDailyTasks(@RequestParam List<UUID> taskIds) {
        dailyRoutineService.deleteDailyTasks(taskIds);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/default-routine/{userId}")
    public ResponseEntity<DefaultRoutine> getDefaultRoutineForUser(@PathVariable UUID userId) {
        DefaultRoutine routine = routineService.getDefaultRoutineByUserId(userId);
        return ResponseEntity.ok(routine);
    }

    @PostMapping("/default-routine/{userId}")
    public ResponseEntity<DefaultRoutine> createDefaultRoutine(
            @PathVariable UUID userId,
            @RequestBody DefaultRoutine defaultRoutine) {
        DefaultRoutine createdRoutine = routineService.createDefaultRoutine(userId, defaultRoutine);
        return ResponseEntity.ok(createdRoutine);
    }

    @PutMapping("/default-routine/{id}")
    public ResponseEntity<DefaultRoutine> updateDefaultRoutine(
            @PathVariable UUID id,
            @RequestBody DefaultRoutine defaultRoutine) {
        DefaultRoutine updatedRoutine = routineService.updateDefaultRoutine(id, defaultRoutine);
        return ResponseEntity.ok(updatedRoutine);
    }

    @GetMapping("/daily-routine/{userId}")
    public ResponseEntity<DailyRoutine> getDailyRoutine(
            @PathVariable UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalTime date) {
        
        LocalTime targetDate = (date != null) ? date : LocalTime.now();
        DailyRoutine dailyRoutine = dailyRoutineService.getOrCreateDailyRoutine(userId, targetDate);
        return ResponseEntity.ok(dailyRoutine);
    }

    // Test Signature
}
