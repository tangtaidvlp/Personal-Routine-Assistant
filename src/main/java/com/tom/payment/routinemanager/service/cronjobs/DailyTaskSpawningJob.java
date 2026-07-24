package com.tom.payment.routinemanager.service.cronjobs;

import java.time.LocalTime;
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
            spawnDailyRoutineForUser(user);
        }
    }

    private void spawnDailyRoutineForUser(User user) {
        LocalTime now = LocalTime.now();
        // Check if weekday or weekend
        boolean isWeekend = now.getDayOfWeek().getValue() >= 6; //
        if (isWeekend) {
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
