package com.woniu.sncp.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.woniu.sncp.security.service.OcpSecurityService;
import com.woniu.sncp.security.web.interceptor.SecurityExceptionHandler;
import com.woniu.sncp.security.web.interceptor.SecurityInterceptor;

@Configuration
@ImportResource({ "classpath*:META-INF/spring/dubbo-security-consumer.xml" })
@ConfigurationProperties(prefix = ""
		+ "", ignoreUnknownFields = true)
@ComponentScan(basePackageClasses=SecurityExceptionHandler.class)
public class OcpSecurityConsumerConfiguration extends WebMvcConfigurerAdapter {

	@Autowired
	private OcpSecurityService ocpSecurityService;

	private String requestIpHeader;

	@Bean
	public SecurityInterceptor securityInterceptor() {
		SecurityInterceptor interceptor = new SecurityInterceptor();
		interceptor.setOcpSecurityService(ocpSecurityService);
		if (StringUtils.hasText(requestIpHeader)) {
			interceptor.setRequestIpHeader(requestIpHeader);
		}
		return interceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(securityInterceptor());
	}

}
