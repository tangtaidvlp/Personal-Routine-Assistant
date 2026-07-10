package com.tom.payment.routinemanager;

import com.tom.payment.routinemanager.model.*;
import com.tom.payment.routinemanager.repository.*;
import com.tom.payment.routinemanager.service.RoutineService;
import com.tom.payment.routinemanager.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RoutineServiceIntegrationTest {

    @Autowired
    private RoutineService routineService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DefaultRoutineRepository defaultRoutineRepository;

    @Autowired
    private DailyRoutineRepository dailyRoutineRepository;

    @Test
    public void testCreateDailyRoutineFromDefault() {
        // 1. Setup User (now automatically creates Default Routine)
        User user = new User("testuser", "test@example.com");
        user = userService.createUser(user);

        // 2. Update Default Routine (instead of creating a new one)
        DefaultRoutine defaultRoutine = new DefaultRoutine();
        defaultRoutine.setName("Workday Routine");

        RoutineTaskTemplate task1 = new RoutineTaskTemplate();
        task1.setName("Morning Coffee");
        task1.setStartTime(LocalTime.of(8, 0));
        task1.setDurationMinutes(15);

        defaultRoutine.setTasks(Collections.singletonList(task1));
        routineService.createDefaultRoutine(user.getId(), defaultRoutine);

        // 3. Trigger Daily Routine creation
        LocalDate today = LocalDate.now();
        DailyRoutine dailyRoutine = routineService.getOrCreateDailyRoutine(user.getId(), today);

        // 4. Assertions
        assertNotNull(dailyRoutine);
        assertEquals(today, dailyRoutine.getDate());
        assertEquals(user.getId(), dailyRoutine.getUser().getId());
        assertEquals(1, dailyRoutine.getTasks().size());
        
        DailyTask dailyTask = dailyRoutine.getTasks().get(0);
        assertEquals("Morning Coffee", dailyTask.getName());
        assertEquals(LocalTime.of(8, 0), dailyTask.getStartTime());
        assertFalse(dailyTask.isCompleted());
    }

    @Test
    public void testUpdateDefaultRoutine() {
        User user = new User("updateuser", "update@example.com");
        user = userService.createUser(user);

        DefaultRoutine existing = defaultRoutineRepository.findByUser(user).orElseThrow();
        UUID routineId = existing.getId();

        DefaultRoutine update = new DefaultRoutine();
        update.setName("Updated Name");
        RoutineTaskTemplate task = new RoutineTaskTemplate();
        task.setName("New Task");
        update.setTasks(Collections.singletonList(task));

        routineService.updateDefaultRoutine(routineId, update);

        DefaultRoutine updated = defaultRoutineRepository.findById(routineId).orElseThrow();
        assertEquals("Updated Name", updated.getName());
        assertEquals(1, updated.getTasks().size());
        assertEquals("New Task", updated.getTasks().get(0).getName());
        assertEquals(updated, updated.getTasks().get(0).getDefaultRoutine());
    }
}
