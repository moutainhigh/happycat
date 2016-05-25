package com.woniu.sncp.nciic.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;

import com.woniu.sncp.nciic.service.NciicClient;

@ConfigurationProperties(prefix = "nciic", ignoreUnknownFields = true)
@Component
public class NciicMessageConfig {

	// @Value("${nciic.redisNodes}")
	private List<String> redisNodes;

	// @Value("${nciic.redisTimeout}")
	private int redisTimeout;

	// @Value("${nciic.redisMaxRedirects}")
	private int redisMaxRedirects;

	// @Value("${nciic.redisIsPool}")
	private boolean redisIsPool = true;

	public List<String> getRedisNodes() {
		return redisNodes;
	}

	public void setRedisNodes(List<String> redisNodes) {
		this.redisNodes = redisNodes;
	}

	public int getRedisTimeout() {
		return redisTimeout;
	}

	public void setRedisTimeout(int redisTimeout) {
		this.redisTimeout = redisTimeout;
	}

	public int getRedisMaxRedirects() {
		return redisMaxRedirects;
	}

	public void setRedisMaxRedirects(int redisMaxRedirects) {
		this.redisMaxRedirects = redisMaxRedirects;
	}

	public boolean isRedisIsPool() {
		return redisIsPool;
	}

	public void setRedisIsPool(boolean redisIsPool) {
		this.redisIsPool = redisIsPool;
	}

	@Bean
	public Jaxb2Marshaller marshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setContextPath("com.woniu.sncp.nciic.wsdl");
		return marshaller;
	}

	@Bean
	public NciicClient nciicCheck() {
		NciicClient nciicClient = new NciicClient();
		return nciicClient;
	}

	@Bean
	public RedisConnectionFactory connectionFactory() {
		RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(redisNodes);
		redisClusterConfiguration.setMaxRedirects(redisMaxRedirects);
		JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisClusterConfiguration);
		jedisConnectionFactory.setTimeout(redisTimeout);
		jedisConnectionFactory.setUsePool(redisIsPool);
		return jedisConnectionFactory;
	}

	public @Bean RedisTemplate<String, String> stringRedisTemplate() {
		RedisTemplate<String, String> template = new RedisTemplate<String, String>();
		template.setConnectionFactory(connectionFactory());
		template.setKeySerializer(new StringRedisSerializer());
		template.setHashValueSerializer(new GenericToStringSerializer<String>(String.class));
		template.setValueSerializer(new GenericToStringSerializer<String>(String.class));
		return template;
	}

}
