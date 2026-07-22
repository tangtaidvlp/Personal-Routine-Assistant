package com.tom.payment.routinemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RoutinemanagerApplication {

	static void main(String[] args) {
		SpringApplication.run(RoutinemanagerApplication.class, args);
	}

}
