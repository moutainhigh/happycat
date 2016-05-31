package cbss.api.nciic.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

import cbss.api.manager.init.listener.ExceptionEventListener;
import cbss.core.trace.aspect.listener.ServletContainerApplicationListener;

@ComponentScan(basePackages = { "cbss.*", "com.*" })
@SpringBootApplication
@ImportResource("classpath*:META-INF/spring/dubbo-passport-consumer.xml")

public class Main {
	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(Main.class);
		application.addListeners(new ExceptionEventListener());
		application.addListeners(new ServletContainerApplicationListener());
		application.run(args);
	}
}
