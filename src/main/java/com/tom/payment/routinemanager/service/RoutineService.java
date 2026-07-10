package com.tom.payment.routinemanager.service;

import com.tom.payment.routinemanager.model.*;
import com.tom.payment.routinemanager.repository.DailyRoutineRepository;
import com.tom.payment.routinemanager.repository.DefaultRoutineRepository;
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

    public RoutineService(UserService userService,
                          DefaultRoutineRepository defaultRoutineRepository,
                          DailyRoutineRepository dailyRoutineRepository) {
        this.userService = userService;
        this.defaultRoutineRepository = defaultRoutineRepository;
        this.dailyRoutineRepository = dailyRoutineRepository;
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
}
