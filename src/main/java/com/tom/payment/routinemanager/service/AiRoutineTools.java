package com.tom.payment.routinemanager.service;

import com.tom.payment.routinemanager.model.*;
import com.tom.payment.routinemanager.repository.DailyRoutineRepository;
import com.tom.payment.routinemanager.repository.DefaultRoutineRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
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
public class AiRoutineTools {

    private final RoutineService routineService;
    private final UserService userService;
    private final DefaultRoutineRepository defaultRoutineRepository;
    private final DailyRoutineRepository dailyRoutineRepository;

    public AiRoutineTools(RoutineService routineService,
                          UserService userService,
                          DefaultRoutineRepository defaultRoutineRepository,
                          DailyRoutineRepository dailyRoutineRepository) {
        this.routineService = routineService;
        this.userService = userService;
        this.defaultRoutineRepository = defaultRoutineRepository;
        this.dailyRoutineRepository = dailyRoutineRepository;
    }

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

    public record TaskUpdateInput(String taskName, String newName, String description, String startTime, Integer durationMinutes) {}

    @Tool(description = "Update one or more existing tasks in the user's default routine template. " +
            "Identify each task by its current taskName. Only provided fields will be changed.")
    public String updateDefaultTasks(
            @ToolParam(description = "The UUID of the user") String userId,
            @ToolParam(description = "List of task updates") List<TaskUpdateInput> updates) {
        try {
            User user = userService.getUserById(UUID.fromString(userId));
            DefaultRoutine routine = defaultRoutineRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Default routine not found for user"));

            List<RoutineTaskTemplate> toUpdate = new ArrayList<>();
            for (TaskUpdateInput u : updates) {
                RoutineTaskTemplate existing = routine.getTasks().stream()
                        .filter(t -> t.getName().equalsIgnoreCase(u.taskName()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Task '" + u.taskName() + "' not found"));

                RoutineTaskTemplate details = new RoutineTaskTemplate();
                details.setId(existing.getId());
                details.setName(u.newName() != null ? u.newName() : existing.getName());
                details.setDescription(u.description() != null ? u.description() : existing.getDescription());
                details.setStartTime(u.startTime() != null ? LocalTime.parse(u.startTime()) : existing.getStartTime());
                details.setDurationMinutes(u.durationMinutes() != null ? u.durationMinutes() : existing.getDurationMinutes());
                toUpdate.add(details);
            }

            routineService.updateDefaultTasks(toUpdate);
            return "Successfully updated " + toUpdate.size() + " task(s) in default routine.";
        } catch (Exception e) {
            return "Failed to update tasks: " + e.getMessage();
        }
    }

    @Tool(description = "Delete one or more tasks from the user's default routine template by their names.")
    public String deleteDefaultTasks(
            @ToolParam(description = "The UUID of the user") String userId,
            @ToolParam(description = "List of task names to delete") List<String> taskNames) {
        try {
            User user = userService.getUserById(UUID.fromString(userId));
            DefaultRoutine routine = defaultRoutineRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Default routine not found for user"));

            List<UUID> ids = routine.getTasks().stream()
                    .filter(t -> taskNames.stream().anyMatch(n -> n.equalsIgnoreCase(t.getName())))
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
            ZonedDateTime targetDate = ZonedDateTime.parse(date);
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

            routineService.addDailyTasks(routine.getId(), entities);
            return "Successfully added " + entities.size() + " task(s) to daily routine for " + date + ".";
        } catch (Exception e) {
            return "Failed to add daily tasks: " + e.getMessage();
        }
    }

    public record DailyTaskUpdateInput(String taskName, String newName, String description, String startTime, Integer durationMinutes, Boolean completed) {}

    @Tool(description = "Update one or more tasks in the user's daily routine for a specific date. " +
            "Identify each task by its current taskName. Can change name, time, duration, or completed status.")
    public String updateDailyTasks(
            @ToolParam(description = "The UUID of the user") String userId,
            @ToolParam(description = "The date in yyyy-MM-dd format") String date,
            @ToolParam(description = "List of daily task updates") List<DailyTaskUpdateInput> updates) {
        try {
            User user = userService.getUserById(UUID.fromString(userId));
            ZonedDateTime targetDate = ZonedDateTime.parse(date);
            DailyRoutine routine = dailyRoutineRepository.findByUserAndDate(user, targetDate)
                    .orElseThrow(() -> new RuntimeException("Daily routine not found for date: " + date));

            List<DailyTask> toUpdate = new ArrayList<>();
            for (DailyTaskUpdateInput u : updates) {
                DailyTask existing = routine.getTasks().stream()
                        .filter(t -> t.getName().equalsIgnoreCase(u.taskName()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Task '" + u.taskName() + "' not found"));

                DailyTask details = new DailyTask();
                details.setId(existing.getId());
                details.setName(u.newName() != null ? u.newName() : existing.getName());
                details.setDescription(u.description() != null ? u.description() : existing.getDescription());
                details.setStartTime(u.startTime() != null ? LocalTime.parse(u.startTime()) : existing.getStartTime());
                details.setDurationMinutes(u.durationMinutes() != null ? u.durationMinutes() : existing.getDurationMinutes());
                details.setCompleted(u.completed() != null ? u.completed() : existing.isCompleted());
                toUpdate.add(details);
            }

            routineService.updateDailyTasks(toUpdate);
            return "Successfully updated " + toUpdate.size() + " task(s) in daily routine for " + date + ".";
        } catch (Exception e) {
            return "Failed to update daily tasks: " + e.getMessage();
        }
    }

    @Tool(description = "Delete one or more tasks from the user's daily routine for a specific date by their names.")
    public String deleteDailyTasks(
            @ToolParam(description = "The UUID of the user") String userId,
            @ToolParam(description = "The date in yyyy-MM-dd format") String date,
            @ToolParam(description = "List of task names to delete") List<String> taskNames) {
        try {
            User user = userService.getUserById(UUID.fromString(userId));
            ZonedDateTime targetDate = ZonedDateTime.parse(date);
            DailyRoutine routine = dailyRoutineRepository.findByUserAndDate(user, targetDate)
                    .orElseThrow(() -> new RuntimeException("Daily routine not found for date: " + date));

            List<UUID> ids = routine.getTasks().stream()
                    .filter(t -> taskNames.stream().anyMatch(n -> n.equalsIgnoreCase(t.getName())))
                    .map(DailyTask::getId)
                    .collect(Collectors.toList());

            if (ids.isEmpty()) return "No matching tasks found to delete.";

            routineService.deleteDailyTasks(ids);
            return "Successfully deleted " + ids.size() + " task(s) from daily routine for " + date + ".";
        } catch (Exception e) {
            return "Failed to delete daily tasks: " + e.getMessage();
        }
    }
}
