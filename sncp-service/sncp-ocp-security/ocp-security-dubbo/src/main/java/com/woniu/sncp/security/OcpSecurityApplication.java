package com.woniu.sncp.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource({"classpath*:META-INF/spring/dubbo-security-provider.xml"})
public class OcpSecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(OcpSecurityApplication.class, args);
	}
}
