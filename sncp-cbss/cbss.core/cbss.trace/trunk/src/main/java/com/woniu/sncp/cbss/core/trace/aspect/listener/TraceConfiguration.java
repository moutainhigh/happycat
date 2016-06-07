package com.woniu.sncp.cbss.core.trace.aspect.listener;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class TraceConfiguration {
	@Bean
	public ThreadPoolTaskExecutor threadPoolTaskScheduler() {
		ThreadPoolTaskExecutor poolTaskScheduler = new ThreadPoolTaskExecutor();
		poolTaskScheduler.setMaxPoolSize(5);
		poolTaskScheduler.setThreadGroupName("TRACEING");
		return poolTaskScheduler;
	}
}
