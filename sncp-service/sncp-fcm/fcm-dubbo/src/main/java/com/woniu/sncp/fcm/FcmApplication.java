package com.woniu.sncp.fcm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * 防沉迷
 * @author chenyx
 * @since JDK1.8
 * @version 1.0.0
 */
@SpringBootApplication
@ImportResource({"classpath*:META-INF/spring/dubbo-fcm-provider.xml",
	"classpath*:META-INF/spring/dubbo-consumer.xml"})
public class FcmApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(FcmApplication.class, args);
	}
	
}
