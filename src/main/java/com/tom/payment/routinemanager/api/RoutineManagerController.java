package com.tom.payment.routinemanager.api;

import com.tom.payment.routinemanager.model.DailyRoutine;
import com.tom.payment.routinemanager.model.DefaultRoutine;
import com.tom.payment.routinemanager.model.User;
import com.tom.payment.routinemanager.service.RoutineService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/routine-manager")
public class RoutineManagerController {

    private final RoutineService routineService;

    public RoutineManagerController(RoutineService routineService) {
        this.routineService = routineService;
    }

    @PostMapping("/default-routine/{userId}")
    public ResponseEntity<DefaultRoutine> createDefaultRoutine(
            @PathVariable UUID userId,
            @RequestBody DefaultRoutine defaultRoutine) {
        DefaultRoutine createdRoutine = routineService.createDefaultRoutine(userId, defaultRoutine);
        return ResponseEntity.ok(createdRoutine);
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
