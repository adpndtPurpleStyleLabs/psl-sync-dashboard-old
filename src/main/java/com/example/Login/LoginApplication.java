package com.example.Login;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableRabbit
@SpringBootApplication
@EnableScheduling
public class LoginApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoginApplication.class, args);
		long heapSize = Runtime.getRuntime().totalMemory();
		long maxHeapSize = Runtime.getRuntime().maxMemory();
		long freeHeapSize = Runtime.getRuntime().freeMemory();

		System.out.println("=================================");
		System.out.println("JVM Heap Memory Details:");
		System.out.println("Initial Heap Size  (Total Memory) : " + (heapSize / 1024 / 1024) + " MB");
		System.out.println("Max Heap Size      : " + (maxHeapSize / 1024 / 1024) + " MB");
		System.out.println("Free Heap Size     : " + (freeHeapSize / 1024 / 1024) + " MB");
		System.out.println("=================================");
	}
}
