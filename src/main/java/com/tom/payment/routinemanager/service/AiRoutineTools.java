package com.tom.payment.routinemanager.service;

import com.tom.payment.routinemanager.model.*;
import com.tom.payment.routinemanager.repository.DailyRoutineRepository;
import com.tom.payment.routinemanager.repository.DefaultRoutineRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Provides @Tool-annotated methods that Gemini can invoke via function calling.
 * Each method is self-contained: it looks up the user/routine by userId,
 * then delegates to RoutineService for the actual persistence.
 */
@Service
@RequiredArgsConstructor
public class AiRoutineTools {

    private final DefaultRoutineService routineService;
    private final DailyRoutineService dailyRoutineService;
    private final UserService userService;
    private final DefaultRoutineRepository defaultRoutineRepository;
    private final DailyRoutineRepository dailyRoutineRepository;

    // ========== Default Routine Tools ==========

    public record TaskInput(String name, String description, String startTime, int durationMinutes) {}

    @Tool(description = "Add one or more tasks to the user's default routine template. " +
            "Each task needs a name, description, startTime (HH:mm format), and durationMinutes.")
    public String addDefaultTasks(
            @ToolParam(description = "The UUID of the user") String userId,
            @ToolParam(description = "List of tasks to add") List<TaskInput> tasks) {
        try {
            User user = userService.getUserById(UUID.fromString(userId));
            DefaultRoutine routine = defaultRoutineRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Default routine not found for user"));

            List<RoutineTaskTemplate> entities = tasks.stream().map(input -> {
                RoutineTaskTemplate t = new RoutineTaskTemplate();
                t.setName(input.name());
                t.setDescription(input.description());
                t.setStartTime(LocalTime.parse(input.startTime()));
                t.setDurationMinutes(input.durationMinutes());
                return t;
            }).collect(Collectors.toList());

            routineService.addDefaultTasks(routine.getId(), entities);
            return "Successfully added " + entities.size() + " task(s) to default routine.";
        } catch (Exception e) {
            return "Failed to add tasks: " + e.getMessage();
        }
    }

        public record TaskUpdateInput(String taskId, String newName, String description, String startTime, Integer durationMinutes) {}

    @Tool(description = "Update one or more existing tasks in the user's default routine template. " +
            "Identify each task by its taskId. Only provided fields will be changed.")
    public String updateDefaultTasks(
            @ToolParam(description = "The UUID of the user") String userId,
            @ToolParam(description = "List of task updates") List<TaskUpdateInput> updates) {
        try {
            User user = userService.getUserById(UUID.fromString(userId));
            DefaultRoutine routine = defaultRoutineRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Default routine not found for user"));

            List<RoutineTaskTemplate> toUpdate = new ArrayList<>();
            for (TaskUpdateInput u : updates) {
                UUID taskId = UUID.fromString(u.taskId());
                RoutineTaskTemplate existing = routine.getTasks().stream()
                    .filter(t -> t.getId().equals(taskId))
                        .findFirst()
                    .orElseThrow(() -> new RuntimeException("Task id '" + u.taskId() + "' not found"));

                RoutineTaskTemplate details = new RoutineTaskTemplate();
                details.setId(existing.getId());
                details.setName(u.newName() != null ? u.newName() : existing.getName());
                details.setDescription(u.description() != null ? u.description() : existing.getDescription());
                details.setStartTime(u.startTime() != null ? LocalTime.parse(u.startTime()) : existing.getStartTime());
                Integer requestedDuration = u.durationMinutes();
                details.setDurationMinutes(requestedDuration != null ? requestedDuration : existing.getDurationMinutes());
                toUpdate.add(details);
            }

            routineService.updateDefaultTasks(toUpdate);
            return "Successfully updated " + toUpdate.size() + " task(s) in default routine.";
        } catch (Exception e) {
            return "Failed to update tasks: " + e.getMessage();
        }
    }

        @Tool(description = "Delete one or more tasks from the user's default routine template by their task IDs.")
    public String deleteDefaultTasks(
            @ToolParam(description = "The UUID of the user") String userId,
            @ToolParam(description = "List of task IDs to delete") List<String> taskIds) {
        try {
            User user = userService.getUserById(UUID.fromString(userId));
            DefaultRoutine routine = defaultRoutineRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Default routine not found for user"));

            List<UUID> requestedIds = taskIds.stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());

            List<UUID> ids = routine.getTasks().stream()
                .filter(t -> requestedIds.contains(t.getId()))
                    .map(RoutineTaskTemplate::getId)
                    .collect(Collectors.toList());

            if (ids.isEmpty()) return "No matching tasks found to delete.";

            routineService.deleteDefaultTasks(ids);
            return "Successfully deleted " + ids.size() + " task(s) from default routine.";
        } catch (Exception e) {
            return "Failed to delete tasks: " + e.getMessage();
        }
    }

    // ========== Daily Routine Tools ==========

    public record DailyTaskInput(String name, String description, String startTime, int durationMinutes, boolean completed) {}

    @Tool(description = "Add one or more tasks to the user's daily routine for a specific date. " +
            "The date must be in yyyy-MM-dd format. Each task needs name, description, startTime (HH:mm), durationMinutes, and completed status.")
    public String addDailyTasks(
            @ToolParam(description = "The UUID of the user") String userId,
            @ToolParam(description = "The date in yyyy-MM-dd format") String date,
            @ToolParam(description = "List of daily tasks to add") List<DailyTaskInput> tasks) {
        try {
            User user = userService.getUserById(UUID.fromString(userId));
            LocalTime targetDate = LocalTime.parse(date);
            DailyRoutine routine = dailyRoutineRepository.findByUserAndDate(user, targetDate)
                    .orElseThrow(() -> new RuntimeException("Daily routine not found for date: " + date));

            List<DailyTask> entities = tasks.stream().map(input -> {
                DailyTask t = new DailyTask();
                t.setName(input.name());
                t.setDescription(input.description());
                t.setStartTime(LocalTime.parse(input.startTime()));
                t.setDurationMinutes(input.durationMinutes());
                t.setCompleted(input.completed());
                return t;
            }).collect(Collectors.toList());

            dailyRoutineService.addDailyTasks(routine.getId(), entities);
            return "Successfully added " + entities.size() + " task(s) to daily routine for " + date + ".";
        } catch (Exception e) {
            return "Failed to add daily tasks: " + e.getMessage();
        }
    }

        public record DailyTaskUpdateInput(String taskId, String newName, String description, String startTime, Integer durationMinutes, Boolean completed) {}

    @Tool(description = "Update one or more tasks in the user's daily routine for a specific date. " +
            "Identify each task by its taskId. Can change name, time, duration, or completed status.")
    public String updateDailyTasks(
            @ToolParam(description = "The UUID of the user") String userId,
            @ToolParam(description = "The date in yyyy-MM-dd format") String date,
            @ToolParam(description = "List of daily task updates") List<DailyTaskUpdateInput> updates) {
        try {
            User user = userService.getUserById(UUID.fromString(userId));
            LocalTime targetDate = LocalTime.parse(date);
            DailyRoutine routine = dailyRoutineRepository.findByUserAndDate(user, targetDate)
                    .orElseThrow(() -> new RuntimeException("Daily routine not found for date: " + date));

            List<DailyTask> toUpdate = new ArrayList<>();
            for (DailyTaskUpdateInput u : updates) {
                UUID taskId = UUID.fromString(u.taskId());
                DailyTask existing = routine.getTasks().stream()
                    .filter(t -> t.getId().equals(taskId))
                        .findFirst()
                    .orElseThrow(() -> new RuntimeException("Task id '" + u.taskId() + "' not found"));

                DailyTask details = new DailyTask();
                details.setId(existing.getId());
                details.setName(u.newName() != null ? u.newName() : existing.getName());
                details.setDescription(u.description() != null ? u.description() : existing.getDescription());
                details.setStartTime(u.startTime() != null ? LocalTime.parse(u.startTime()) : existing.getStartTime());
                Integer requestedDuration = u.durationMinutes();
                details.setDurationMinutes(requestedDuration != null ? requestedDuration : existing.getDurationMinutes());
                Boolean requestedCompleted = u.completed();
                details.setCompleted(requestedCompleted != null ? requestedCompleted : existing.isCompleted());
                toUpdate.add(details);
            }

            dailyRoutineService.updateDailyTasks(toUpdate);
            return "Successfully updated " + toUpdate.size() + " task(s) in daily routine for " + date + ".";
        } catch (Exception e) {
            return "Failed to update daily tasks: " + e.getMessage();
        }
    }

        @Tool(description = "Delete one or more tasks from the user's daily routine for a specific date by their task IDs.")
    public String deleteDailyTasks(
            @ToolParam(description = "The UUID of the user") String userId,
            @ToolParam(description = "The date in yyyy-MM-dd format") String date,
            @ToolParam(description = "List of task IDs to delete") List<String> taskIds) {
        try {
            User user = userService.getUserById(UUID.fromString(userId));
            LocalTime targetDate = LocalTime.parse(date);
            DailyRoutine routine = dailyRoutineRepository.findByUserAndDate(user, targetDate)
                    .orElseThrow(() -> new RuntimeException("Daily routine not found for date: " + date));

            List<UUID> requestedIds = taskIds.stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());

            List<UUID> ids = routine.getTasks().stream()
                .filter(t -> requestedIds.contains(t.getId()))
                    .map(DailyTask::getId)
                    .collect(Collectors.toList());

            if (ids.isEmpty()) return "No matching tasks found to delete.";

            dailyRoutineService.deleteDailyTasks(ids);
            return "Successfully deleted " + ids.size() + " task(s) from daily routine for " + date + ".";
        } catch (Exception e) {
            return "Failed to delete daily tasks: " + e.getMessage();
        }
    }
}
