package com.woniu.sncp.fcm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource({"classpath*:META-INF/spring/fcm-schedule-integration.xml"})
public class ScheduleApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(ScheduleApplication.class, args);
	}
}
