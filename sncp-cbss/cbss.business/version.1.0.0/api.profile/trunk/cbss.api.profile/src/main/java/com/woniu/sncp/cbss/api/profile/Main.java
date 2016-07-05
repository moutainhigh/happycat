package com.woniu.sncp.cbss.api.profile;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.FileSystemResource;

import com.woniu.sncp.cbss.api.manager.init.listener.AppStateFailedListener;
import com.woniu.sncp.cbss.api.manager.init.listener.AppStatePreparedListener;
import com.woniu.sncp.cbss.api.manager.init.listener.AppStateReadyListener;
import com.woniu.sncp.cbss.api.manager.init.listener.AppStateStartedListener;
import com.woniu.sncp.cbss.api.manager.init.listener.ExceptionEventListener;
import com.woniu.sncp.cbss.api.profile.config.EncryptUtils;
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
		final String keyPath="/opt/security/db/db_key.dat";
		final String encryptedPath="/opt/security/db/mysql.properties";
        FileSystemResource keyResource = new FileSystemResource(keyPath);
        FileSystemResource dbPropResource = new FileSystemResource(encryptedPath);
        
        InputStream is = null;
        try {
	        Key key = EncryptUtils.getKey(keyResource.getInputStream());
	        is = EncryptUtils.doDecrypt(key, dbPropResource.getInputStream());
	        
	        Properties prop = new Properties();
	        prop.load(is);
	        application.setDefaultProperties(prop);
        } catch (IOException e) {
		} finally {
            if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
        }
		
		application.run(args);
	}
}
