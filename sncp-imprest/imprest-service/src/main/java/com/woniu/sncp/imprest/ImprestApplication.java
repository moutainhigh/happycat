package com.woniu.sncp.imprest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * 充值服务主类
 * @author chenyx
 *
 */
@SpringBootApplication
@ImportResource({"classpath*:META-INF/spring/dubbo-imprest-provider.xml"})
public class ImprestApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImprestApplication.class, args);
	}
}
