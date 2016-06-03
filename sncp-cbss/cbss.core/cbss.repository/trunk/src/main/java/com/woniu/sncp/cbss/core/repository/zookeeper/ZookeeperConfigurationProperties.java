package com.woniu.sncp.cbss.core.repository.zookeeper;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cbss.api.zookeeper.conf", locations = { "classpath:zookeeper.properties" })
public class ZookeeperConfigurationProperties {

	private String nodes = "";
	private int maxRetrie = 3;
	private int baseSleepTimeMS = 3000;
	private String nameSpace = "cfg";
	private int threadPoolSize = 5;

	public String getNodes() {
		return nodes;
	}

	public void setNodes(String nodes) {
		this.nodes = nodes;
	}

	public int getMaxRetrie() {
		return maxRetrie;
	}

	public void setMaxRetrie(int maxRetrie) {
		this.maxRetrie = maxRetrie;
	}

	public int getBaseSleepTimeMS() {
		return baseSleepTimeMS;
	}

	public void setBaseSleepTimeMS(int baseSleepTimeMS) {
		this.baseSleepTimeMS = baseSleepTimeMS;
	}

	public String getNameSpace() {
		return nameSpace;
	}

	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}

	public int getThreadPoolSize() {
		return threadPoolSize;
	}

	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

}
