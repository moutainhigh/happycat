package com.woniu.sncp.security.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "security.zookeeper", ignoreInvalidFields = true)
public class ZookeeperPropertyConfiguration {
	
	//zookeeper连接字符串
	private String connectString;
	//超时时间
	private int sessionTimeoutMs;
	//连接超时时间
	private int connectionTimeoutMs;
	//重连间隔时间
	private int baseSleepTimeMs;
	//重连最大重试次数
	private int maxRetries;
	
	@Bean
	public CuratorFramework curatorFramework() {
		CuratorFramework client = CuratorFrameworkFactory.builder()
				.connectString(connectString)
				.sessionTimeoutMs(sessionTimeoutMs)
				.connectionTimeoutMs(connectionTimeoutMs)
				.retryPolicy((new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries)))
				.build();
		return client;
	}

	public String getConnectString() {
		return connectString;
	}

	public void setConnectString(String connectString) {
		this.connectString = connectString;
	}

	public int getSessionTimeoutMs() {
		return sessionTimeoutMs;
	}

	public void setSessionTimeoutMs(int sessionTimeoutMs) {
		this.sessionTimeoutMs = sessionTimeoutMs;
	}
	
	public int getConnectionTimeoutMs() {
		return connectionTimeoutMs;
	}

	public void setConnectionTimeoutMs(int connectionTimeoutMs) {
		this.connectionTimeoutMs = connectionTimeoutMs;
	}

	public int getBaseSleepTimeMs() {
		return baseSleepTimeMs;
	}

	public void setBaseSleepTimeMs(int baseSleepTimeMs) {
		this.baseSleepTimeMs = baseSleepTimeMs;
	}

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}
	
}
