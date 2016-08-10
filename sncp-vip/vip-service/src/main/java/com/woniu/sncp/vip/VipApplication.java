package com.woniu.sncp.vip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource({"classpath*:META-INF/spring/dubbo-vip-provider.xml"})
public class VipApplication {

	public static void main(String[] args) {
		SpringApplication.run(VipApplication.class, args);
	}
}
