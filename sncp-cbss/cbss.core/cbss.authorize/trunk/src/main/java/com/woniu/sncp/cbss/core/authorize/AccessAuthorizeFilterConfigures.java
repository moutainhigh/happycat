package com.woniu.sncp.cbss.core.authorize;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class AccessAuthorizeFilterConfigures {

	public static final String BASE_CONTEXT = "/cbss/api";
	public static final String BASE_CONTEXT_FILTER = BASE_CONTEXT + "/*";

	@Autowired
	private AutowireCapableBeanFactory autowireCapableBeanFactory;

	@Autowired
	public AccessAuthorizeFilter accessAuthorizeFilter;

	@Bean
	public FilterRegistrationBean addAccessAuthorizeFilter() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(accessAuthorizeFilter);
		registration.addUrlPatterns(BASE_CONTEXT_FILTER);
		registration.setName("accessAuthorizeFilter");
		registration.setOrder(0);
		return registration;
	}
}
