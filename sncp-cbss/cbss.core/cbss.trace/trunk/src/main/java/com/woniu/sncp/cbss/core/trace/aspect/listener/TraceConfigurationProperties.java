package com.woniu.sncp.cbss.core.trace.aspect.listener;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cbss.api.trace.conf", locations = { "classpath:zookeeper.properties" })
public class TraceConfigurationProperties {
	
	private int exceptionStackTraceDeepLength = 5;

	public int getExceptionStackTraceDeepLength() {
		return exceptionStackTraceDeepLength;
	}

	public void setExceptionStackTraceDeepLength(int exceptionStackTraceDeepLength) {
		this.exceptionStackTraceDeepLength = exceptionStackTraceDeepLength;
	}
	
	
	
}
