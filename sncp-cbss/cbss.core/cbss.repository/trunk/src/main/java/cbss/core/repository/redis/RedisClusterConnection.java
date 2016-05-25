package cbss.core.repository.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisClusterConnection {
	/**
	 * Type safe representation of application.properties
	 */
	@Autowired
	private RedisClusterConfigurationProperties clusterProperties;

	public @Bean RedisConnectionFactory connectionFactory() {
		RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(clusterProperties.getNodes());
		redisClusterConfiguration.setMaxRedirects(clusterProperties.getMaxRedirects());
		JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisClusterConfiguration);
		jedisConnectionFactory.setTimeout(clusterProperties.getTimeout());
		jedisConnectionFactory.setUsePool(clusterProperties.isPool());
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
