package com.woniu.sncp.alarm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource({"classpath*:META-INF/spring/dubbo-alarm-provider.xml"})
public class AlarmApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlarmApplication.class, args);
	}
	
}
