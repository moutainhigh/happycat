package com.woniu.sncp.profile;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.FileSystemResource;

import com.woniu.sncp.cbss.api.manager.init.listener.AppStateFailedListener;
import com.woniu.sncp.cbss.api.manager.init.listener.AppStatePreparedListener;
import com.woniu.sncp.cbss.api.manager.init.listener.AppStateReadyListener;
import com.woniu.sncp.cbss.api.manager.init.listener.AppStateStartedListener;
import com.woniu.sncp.cbss.api.manager.init.listener.ExceptionEventListener;
import com.woniu.sncp.cbss.core.trace.aspect.listener.ServletContainerApplicationListener;
import com.woniu.sncp.profile.config.EncryptUtils;

@SpringBootApplication
@ImportResource({"classpath*:META-INF/spring/*.xml"})
public class ProfileApplication {
	
	public static void main(String[] args) {
		final String keyPath="/opt/security/db/db_key.dat";
		final String encryptedPath="/opt/security/db/mysql.properties";
		
		SpringApplication application = new SpringApplication(ProfileApplication.class);
		application.addListeners(new ExceptionEventListener());
		application.addListeners(new ServletContainerApplicationListener());
		application.addListeners(new AppStateStartedListener());
		application.addListeners(new AppStateFailedListener());
		application.addListeners(new AppStatePreparedListener());
		application.addListeners(new AppStateReadyListener());
		
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
		
//		SpringApplication.run(ProfileApplication.class, args);
	}
	
}
