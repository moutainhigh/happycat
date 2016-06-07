package com.woniu.sncp.passport.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.PingUrl;

public class PassportConfiguration {
	
	@Autowired
	IClientConfig ribbonClientConfig;

	@Bean
	public IPing ribbonPing(IClientConfig config) {
		return new PingUrl(false,"/doc");
	}

}
