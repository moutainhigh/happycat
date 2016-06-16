package com.woniu.sncp.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;

@Configuration
public class OcpSecurityConfiguration {

	@Bean
	public PasswordEncoder passwordEncoder() {
		Md5PasswordEncoder md5PasswordEncoder = new Md5PasswordEncoder();
		return md5PasswordEncoder;
	}
}
