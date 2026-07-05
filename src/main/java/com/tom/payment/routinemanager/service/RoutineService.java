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
        
        defaultRoutine.setUser(user);
        if (defaultRoutine.getTasks() != null) {
            defaultRoutine.getTasks().forEach(task -> task.setDefaultRoutine(defaultRoutine));
        }
        
        return defaultRoutineRepository.save(defaultRoutine);
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
