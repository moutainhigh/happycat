package cbss.api.manager.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

import de.codecentric.boot.admin.config.EnableAdminServer;

@ComponentScan(basePackages = { "cbss.*" })
@SpringBootApplication
@EnableAdminServer
@ImportResource("classpath*:META-INF/spring/dubbo-passport-consumer.xml")
public class ManagerMain {
	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(ManagerMain.class);
		application.addListeners(new ExceptionEventListener());
		application.run(args);
	}

}
