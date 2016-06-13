package com.woniu.sncp.passport.Health;

import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class RedisHealthIndicator  extends AbstractHealthIndicator {
	
	private RedisConnectionFactory redisConnectionFactory;
	
	@Autowired
	public RedisHealthIndicator(RedisConnectionFactory connectionFactory) {
		Assert.notNull(connectionFactory, "ConnectionFactory must not be null");
		this.redisConnectionFactory = connectionFactory;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		RedisConnection connection = RedisConnectionUtils
				.getConnection(this.redisConnectionFactory);
		try {
			Properties info = connection.info();
			StringBuffer buffer = new StringBuffer();
			Set<Object> keySet = info.keySet();
			for(Object k : keySet) {
				String key = String.valueOf(k);
				if(key.lastIndexOf("redis_version") != -1) {
					buffer.append(key + ":" + info.get(key) + " ");
				}
			}
			builder.up().withDetail("state", buffer.toString());
		}
		finally {
			RedisConnectionUtils.releaseConnection(connection,
					this.redisConnectionFactory);
		}
	}


}
