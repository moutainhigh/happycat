package com.bigdullrock.spring.boot.nifty.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("nifty")
public class NiftyServerProperties {

	private Integer port;
	private Integer maxConnections;
	private Integer clientIdleTimeout;
	private Integer taskTimeout;
	private Integer queueTimeout;
	private Integer queuedResponseLimit;

	public Integer getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(Integer maxConnections) {
		this.maxConnections = maxConnections;
	}

	public Integer getClientIdleTimeout() {
		return clientIdleTimeout;
	}

	public void setClientIdleTimeout(Integer clientIdleTimeout) {
		this.clientIdleTimeout = clientIdleTimeout;
	}

	public Integer getTaskTimeout() {
		return taskTimeout;
	}

	public void setTaskTimeout(Integer taskTimeout) {
		this.taskTimeout = taskTimeout;
	}

	public Integer getQueueTimeout() {
		return queueTimeout;
	}

	public void setQueueTimeout(Integer queueTimeout) {
		this.queueTimeout = queueTimeout;
	}

	public Integer getQueuedResponseLimit() {
		return queuedResponseLimit;
	}

	public void setQueuedResponseLimit(Integer queuedResponseLimit) {
		this.queuedResponseLimit = queuedResponseLimit;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(final Integer port) {
		this.port = port;
	}
}
