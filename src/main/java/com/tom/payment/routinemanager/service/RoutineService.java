package com.tom.payment.routinemanager.service;

import com.tom.payment.routinemanager.model.*;
import com.tom.payment.routinemanager.repository.DailyRoutineRepository;
import com.tom.payment.routinemanager.repository.DefaultRoutineRepository;
import com.tom.payment.routinemanager.repository.DailyTaskRepository;
import com.tom.payment.routinemanager.repository.RoutineTaskTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoutineService {

    private final UserService userService;
    private final DefaultRoutineRepository defaultRoutineRepository;
    private final DailyRoutineRepository dailyRoutineRepository;
    private final RoutineTaskTemplateRepository routineTaskTemplateRepository;
    private final DailyTaskRepository dailyTaskRepository;

    public RoutineService(UserService userService,
                          DefaultRoutineRepository defaultRoutineRepository,
                          DailyRoutineRepository dailyRoutineRepository,
                          RoutineTaskTemplateRepository routineTaskTemplateRepository,
                          DailyTaskRepository dailyTaskRepository) {
        this.userService = userService;
        this.defaultRoutineRepository = defaultRoutineRepository;
        this.dailyRoutineRepository = dailyRoutineRepository;
        this.routineTaskTemplateRepository = routineTaskTemplateRepository;
        this.dailyTaskRepository = dailyTaskRepository;
    }

    @Transactional
    public DefaultRoutine createDefaultRoutine(UUID userId, DefaultRoutine defaultRoutine) {
        User user = userService.getUserById(userId);
        
        // If a default routine already exists for this user, we might want to update it instead or throw error.
        // For simplicity, let's just use findByUser.
        return defaultRoutineRepository.findByUser(user)
                .map(existing -> {
                    existing.setName(defaultRoutine.getName());
                    existing.getTasks().clear();
                    if (defaultRoutine.getTasks() != null) {
                        defaultRoutine.getTasks().forEach(task -> {
                            task.setDefaultRoutine(existing);
                            existing.getTasks().add(task);
                        });
                    }
                    return defaultRoutineRepository.save(existing);
                })
                .orElseGet(() -> {
                    defaultRoutine.setUser(user);
                    if (defaultRoutine.getTasks() != null) {
                        defaultRoutine.getTasks().forEach(task -> task.setDefaultRoutine(defaultRoutine));
                    }
                    return defaultRoutineRepository.save(defaultRoutine);
                });
    }

    @Transactional
    public DefaultRoutine updateDefaultRoutine(UUID id, DefaultRoutine defaultRoutine) {
        DefaultRoutine existing = defaultRoutineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Default routine not found"));
        
        existing.setName(defaultRoutine.getName());
        
        // Simple update: clear and add new tasks
        existing.getTasks().clear();
        if (defaultRoutine.getTasks() != null) {
            defaultRoutine.getTasks().forEach(task -> {
                task.setDefaultRoutine(existing);
                existing.getTasks().add(task);
            });
        }
        
        return defaultRoutineRepository.save(existing);
    }

    @Transactional
    public DailyRoutine getOrCreateDailyRoutine(UUID userId, LocalDate date) {
        User user = userService.getUserById(userId);

        return dailyRoutineRepository.findByUserAndDate(user, date)
                .orElseGet(() -> createDailyRoutineFromDefault(user, date));
    }

    private DailyRoutine createDailyRoutineFromDefault(User user, LocalDate date) {
        DefaultRoutine defaultRoutine = defaultRoutineRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Default routine not found for user"));

        DailyRoutine dailyRoutine = new DailyRoutine();
        dailyRoutine.setUser(user);
        dailyRoutine.setDate(date);

        List<DailyTask> dailyTasks = defaultRoutine.getTasks().stream()
                .map(template -> {
                    DailyTask task = new DailyTask();
                    task.setName(template.getName());
                    task.setDescription(template.getDescription());
                    task.setStartTime(template.getStartTime());
                    task.setDurationMinutes(template.getDurationMinutes());
                    task.setCompleted(false);
                    task.setDailyRoutine(dailyRoutine);
                    return task;
                })
                .collect(Collectors.toList());

        dailyRoutine.setTasks(dailyTasks);
        return dailyRoutineRepository.save(dailyRoutine);
    }

    @Transactional
    public List<RoutineTaskTemplate> addDefaultTasks(UUID defaultRoutineId, List<RoutineTaskTemplate> tasks) {
        DefaultRoutine routine = defaultRoutineRepository.findById(defaultRoutineId)
                .orElseThrow(() -> new RuntimeException("Default routine not found"));
        tasks.forEach(task -> task.setDefaultRoutine(routine));
        return routineTaskTemplateRepository.saveAll(tasks);
    }

    @Transactional
    public List<RoutineTaskTemplate> updateDefaultTasks(List<RoutineTaskTemplate> tasksDetails) {
        return tasksDetails.stream().map(details -> {
            RoutineTaskTemplate existing = routineTaskTemplateRepository.findById(details.getId())
                    .orElseThrow(() -> new RuntimeException("Task template not found: " + details.getId()));
            existing.setName(details.getName());
            existing.setDescription(details.getDescription());
            existing.setStartTime(details.getStartTime());
            existing.setDurationMinutes(details.getDurationMinutes());
            return routineTaskTemplateRepository.save(existing);
        }).collect(Collectors.toList());
    }

    @Transactional
    public void deleteDefaultTasks(List<UUID> taskIds) {
        List<RoutineTaskTemplate> tasks = routineTaskTemplateRepository.findAllById(taskIds);
        tasks.forEach(task -> {
            if (task.getDefaultRoutine() != null) {
                task.getDefaultRoutine().getTasks().remove(task);
            }
        });
        routineTaskTemplateRepository.deleteAll(tasks);
    }

    @Transactional
    public List<DailyTask> addDailyTasks(UUID dailyRoutineId, List<DailyTask> tasks) {
        DailyRoutine routine = dailyRoutineRepository.findById(dailyRoutineId)
                .orElseThrow(() -> new RuntimeException("Daily routine not found"));
        tasks.forEach(task -> task.setDailyRoutine(routine));
        return dailyTaskRepository.saveAll(tasks);
    }

    @Transactional
    public List<DailyTask> updateDailyTasks(List<DailyTask> tasksDetails) {
        return tasksDetails.stream().map(details -> {
            DailyTask existing = dailyTaskRepository.findById(details.getId())
                    .orElseThrow(() -> new RuntimeException("Daily task not found: " + details.getId()));
            existing.setName(details.getName());
            existing.setDescription(details.getDescription());
            existing.setStartTime(details.getStartTime());
            existing.setDurationMinutes(details.getDurationMinutes());
            existing.setCompleted(details.isCompleted());
            return dailyTaskRepository.save(existing);
        }).collect(Collectors.toList());
    }

    @Transactional
    public void deleteDailyTasks(List<UUID> taskIds) {
        List<DailyTask> tasks = dailyTaskRepository.findAllById(taskIds);
        tasks.forEach(task -> {
            if (task.getDailyRoutine() != null) {
                task.getDailyRoutine().getTasks().remove(task);
            }
        });
        dailyTaskRepository.deleteAll(tasks);
    }
}
