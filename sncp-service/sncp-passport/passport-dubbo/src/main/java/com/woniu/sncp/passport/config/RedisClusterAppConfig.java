package com.woniu.sncp.passport.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.woniu.snco.passport.entity.PassportEntity;

import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisClusterAppConfig {

	@Autowired
	RedisClusterConfigurationProperties clusterProperties;

	@Bean
	public RedisConnectionFactory connectionFactory() {
		JedisConnectionFactory connectionFactory = new JedisConnectionFactory(new RedisClusterConfiguration(clusterProperties.getNodes()));
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMinIdle(clusterProperties.getMinIdle());
		poolConfig.setMaxTotal(clusterProperties.getMaxTotal());
		poolConfig.setMaxWaitMillis(clusterProperties.getMaxWaitMillis());
		connectionFactory.setPoolConfig(poolConfig);
		return connectionFactory;
	}
	
	@Bean(name="passportRedisTemplate")
	public RedisTemplate<String, PassportEntity> passportRedisTemplate() {
		RedisTemplate<String, PassportEntity> redisTemplate = new RedisTemplate<String, PassportEntity>();
		redisTemplate.setConnectionFactory(connectionFactory());
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<PassportEntity>(PassportEntity.class));
		return redisTemplate;
	}
}
