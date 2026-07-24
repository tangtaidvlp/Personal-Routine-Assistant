package com.tom.payment.routinemanager.service;

import java.util.List;

import com.tom.payment.routinemanager.model.DefaultRoutine;
import com.tom.payment.routinemanager.model.User;
import com.tom.payment.routinemanager.repository.UserRepository;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DefaultRoutineService routineService;

    public UserService(UserRepository userRepository, @Lazy DefaultRoutineService routineService) {
        this.userRepository = userRepository;
        this.routineService = routineService;
    }

    @Transactional
    public User createUserAndDefaultDailyRoutine(User user) {
        User savedUser = userRepository.save(user);
        
        DefaultRoutine defaultRoutine = new DefaultRoutine();
        defaultRoutine.setName("Default Routine");
        defaultRoutine.setRoutineType(DefaultRoutine.RoutineTypeEnum.DAILY);
        routineService.createDefaultRoutine(savedUser.getId(), defaultRoutine);
        
        return savedUser;
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Fetch all users
    public List<User> getAllUsers() {
        return (List<User>) userRepository.findAll();
    }
}
