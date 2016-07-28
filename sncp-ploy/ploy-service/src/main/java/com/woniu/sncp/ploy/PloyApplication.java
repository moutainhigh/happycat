package com.woniu.sncp.ploy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource({"classpath*:META-INF/spring/main-ploy-flow.xml"})
public class PloyApplication {

	public static void main(String[] args) {
		SpringApplication.run(PloyApplication.class, args);
	}
}
