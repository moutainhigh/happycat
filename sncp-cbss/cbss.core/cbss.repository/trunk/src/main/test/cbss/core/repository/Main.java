package cbss.core.repository;

import java.io.UnsupportedEncodingException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@SpringBootApplication
public class Main {
	public static void main(String[] args) throws UnsupportedEncodingException, Exception {
		SpringApplication.run(Main.class, args);
	}
}
