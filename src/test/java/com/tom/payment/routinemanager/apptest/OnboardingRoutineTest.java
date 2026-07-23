package com.tom.payment.routinemanager.apptest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import com.tom.payment.routinemanager.model.RoutineTaskTemplate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@SpringBootTest
public class OnboardingRoutineTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void onboardingWorkflowCreatesUserAndDefaultRoutineWithTasks() throws Exception {
        Map<String, Object> newUser = Map.of(
                "username", "onboarding-user",
                "email", "onboarding@example.com",
                "passwordHash", "hashed-password"
        );

        MvcResult createUserResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode createdUser = objectMapper.readTree(createUserResult.getResponse().getContentAsString());
        assertNotNull(createdUser);
        String userId = createdUser.path("id").asText();
        assertNotNull(userId);

        MvcResult defaultRoutineResult = mockMvc.perform(get("/api/routine-manager/default-routine/{userId}", userId))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode defaultRoutine = objectMapper.readTree(defaultRoutineResult.getResponse().getContentAsString());
        assertNotNull(defaultRoutine);
        assertEquals("Default Routine", defaultRoutine.path("name").asText());
        String routineId = defaultRoutine.path("id").asText();
        assertNotNull(routineId);

        RoutineTaskTemplate taskOne = new RoutineTaskTemplate();
        taskOne.setName("Morning Stretch");
        taskOne.setDescription("Light stretching routine");
        taskOne.setStartTime(LocalTime.of(7, 0));
        taskOne.setDurationMinutes(15);

        RoutineTaskTemplate taskTwo = new RoutineTaskTemplate();
        taskTwo.setName("Plan Day");
        taskTwo.setDescription("Review priorities for the day");
        taskTwo.setStartTime(LocalTime.of(8, 0));
        taskTwo.setDurationMinutes(30);

        MvcResult addTasksResult = mockMvc.perform(post("/api/routine-manager/default-routine/{routineId}/tasks", routineId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(taskOne, taskTwo))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode createdTasks = objectMapper.readTree(addTasksResult.getResponse().getContentAsString());
        assertNotNull(createdTasks);
        assertEquals(2, createdTasks.size());
        assertEquals("Morning Stretch", createdTasks.get(0).path("name").asText());
        assertEquals("Plan Day", createdTasks.get(1).path("name").asText());
    }

    @Test
    void addDefaultTasksAcceptsSingleTaskObjectPayload() throws Exception {
        Map<String, Object> newUser = Map.of(
                "username", "single-payload-user",
                "email", "single-payload@example.com",
                "passwordHash", "hashed-password"
        );

        MvcResult createUserResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode createdUser = objectMapper.readTree(createUserResult.getResponse().getContentAsString());
        String userId = createdUser.path("id").asText();
        assertNotNull(userId);

        MvcResult defaultRoutineResult = mockMvc.perform(get("/api/routine-manager/default-routine/{userId}", userId))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode defaultRoutine = objectMapper.readTree(defaultRoutineResult.getResponse().getContentAsString());
        String routineId = defaultRoutine.path("id").asText();
        assertNotNull(routineId);

        RoutineTaskTemplate task = new RoutineTaskTemplate();
        task.setName("Single Task");
        task.setDescription("Accepts a single object payload");
        task.setStartTime(LocalTime.of(9, 0));
        task.setDurationMinutes(20);

        MvcResult addTasksResult = mockMvc.perform(post("/api/routine-manager/default-routine/{routineId}/tasks", routineId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode createdTasks = objectMapper.readTree(addTasksResult.getResponse().getContentAsString());
        assertNotNull(createdTasks);
        assertEquals(1, createdTasks.size());
        assertEquals("Single Task", createdTasks.get(0).path("name").asText());
    }
}
