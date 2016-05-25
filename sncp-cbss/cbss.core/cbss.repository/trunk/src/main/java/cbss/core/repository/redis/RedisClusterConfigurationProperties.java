package cbss.core.repository.redis;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cbss.api.redis.cluster", locations = { "classpath:redis.properties" }, ignoreUnknownFields = true)
public class RedisClusterConfigurationProperties {
	/*
	 * spring.redis.cluster.nodes[0] = 127.0.0.1:7379
	 * spring.redis.cluster.nodes[1] = 127.0.0.1:7380 ...
	 */
	private List<String> nodes;
	private int timeout;
	private int maxRedirects;
	private boolean isPool = true;

	public boolean isPool() {
		return isPool;
	}

	public void setPool(boolean isPool) {
		this.isPool = isPool;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getMaxRedirects() {
		return maxRedirects;
	}

	public void setMaxRedirects(int maxRedirects) {
		this.maxRedirects = maxRedirects;
	}

	/**
	 * Get initial collection of known cluster nodes in format {@code host:port}
	 * 
	 * @return
	 */
	public List<String> getNodes() {
		return nodes;
	}

	public void setNodes(List<String> nodes) {
		this.nodes = nodes;
	}
}
