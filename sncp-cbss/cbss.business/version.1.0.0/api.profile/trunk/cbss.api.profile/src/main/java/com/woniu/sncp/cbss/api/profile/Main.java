package com.woniu.sncp.cbss.api.profile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

import com.woniu.sncp.cbss.api.manager.init.listener.AppStateFailedListener;
import com.woniu.sncp.cbss.api.manager.init.listener.AppStatePreparedListener;
import com.woniu.sncp.cbss.api.manager.init.listener.AppStateReadyListener;
import com.woniu.sncp.cbss.api.manager.init.listener.AppStateStartedListener;
import com.woniu.sncp.cbss.api.manager.init.listener.ExceptionEventListener;
import com.woniu.sncp.cbss.core.trace.aspect.listener.ServletContainerApplicationListener;

@ComponentScan(basePackages = { "com.woniu.sncp.*" })
@SpringBootApplication
@ImportResource("classpath*:META-INF/spring/dubbo-consumer.xml")

public class Main {
	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(Main.class);
		application.addListeners(new ExceptionEventListener());
		application.addListeners(new ServletContainerApplicationListener());
		application.addListeners(new AppStateStartedListener());
		application.addListeners(new AppStateFailedListener());
		application.addListeners(new AppStatePreparedListener());
		application.addListeners(new AppStateReadyListener());
		application.run(args);
	}
}
