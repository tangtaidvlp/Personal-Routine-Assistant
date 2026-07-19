package com.tom.payment.routinemanager.api;

import com.tom.payment.routinemanager.model.DailyTask;
import com.tom.payment.routinemanager.model.RoutineTaskTemplate;
import com.tom.payment.routinemanager.model.DailyRoutine;
import com.tom.payment.routinemanager.model.DefaultRoutine;
import com.tom.payment.routinemanager.model.User;
import com.tom.payment.routinemanager.service.RoutineService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.tom.payment.routinemanager.dto.ChatRequest;
import com.tom.payment.routinemanager.dto.ChatResponse;
import com.tom.payment.routinemanager.service.AiChatService;

@RestController
@RequestMapping("/api/routine-manager")
public class RoutineManagerController {

    private final RoutineService routineService;
    private final AiChatService aiChatService;

    public RoutineManagerController(RoutineService routineService, AiChatService aiChatService) {
        this.routineService = routineService;
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
            @RequestBody List<RoutineTaskTemplate> tasks) {
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
        List<DailyTask> createdTasks = routineService.addDailyTasks(routineId, tasks);
        return ResponseEntity.ok(createdTasks);
    }

    @PutMapping("/daily-routine/tasks")
    public ResponseEntity<List<DailyTask>> updateDailyTasks(
            @RequestBody List<DailyTask> tasksDetails) {
        List<DailyTask> updatedTasks = routineService.updateDailyTasks(tasksDetails);
        return ResponseEntity.ok(updatedTasks);
    }

    @DeleteMapping("/daily-routine/tasks")
    public ResponseEntity<Void> deleteDailyTasks(@RequestParam List<UUID> taskIds) {
        routineService.deleteDailyTasks(taskIds);
        return ResponseEntity.noContent().build();
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        DailyRoutine dailyRoutine = routineService.getOrCreateDailyRoutine(userId, targetDate);
        return ResponseEntity.ok(dailyRoutine);
    }
}
