package com.woniu.sncp.cbss.core.repository.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class RedisHealth implements HealthIndicator {

	@Autowired
	private RedisService redisService;

	@Override
	public Health health() {
		Builder build = null;
		try {
			boolean is = redisService.isAlive();
			if (is) {
				build = Health.up();
				build.withDetail("info", String.format("state:%S,info:%S", is, redisService.redisInfo()));
			} else {
				build = Health.down();
				build.withDetail("info", is);
			}
		} catch (Exception e) {
			build = Health.unknown();
			build.withDetail("Exception", e.getMessage());
		}

		return build.build();
	}
}
