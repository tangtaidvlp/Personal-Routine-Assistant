package com.tom.payment.routinemanager.service.cronjobs;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tom.payment.routinemanager.model.User;
import com.tom.payment.routinemanager.service.DailyRoutineService;
import com.tom.payment.routinemanager.service.UserService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DailyTaskSpawningJob {

    private final UserService userService;
    private final DailyRoutineService dailyRoutineService;

    @Scheduled(cron = "0 0 0 * * ?") // This cron expression means "at midnight every day"
    public void spawnDailyRoutine() {
        List<User> users = userService.getAllUsers();
        for (User user : users) {
            try {
                spawnDailyRoutineForUser(user);
            } catch (Exception e) {
                // Log the error and continue with the next user
                System.err.println("Error spawning daily routine for user " + user.getId() + ": " + e.getMessage());
            }
        }
    }

    private void spawnDailyRoutineForUser(User user) {
        LocalDate now = LocalDate.now();
        // Check if weekday or weekend
        boolean hasWeekendRoutine = now.getDayOfWeek().getValue() >= 6;
        if (hasWeekendRoutine) {
            if (user.getWeekendDefaultRoutine().isPresent()) {
                dailyRoutineService.spawnWeekendRoutineForUser(user, user.getWeekendDefaultRoutine().get());
            } else {
                dailyRoutineService.spawnWeekdayRoutineForUser(user);
            }
        } else {
            dailyRoutineService.spawnWeekdayRoutineForUser(user);
        }
    }

    
}
