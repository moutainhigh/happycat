package com.woniu.sncp.cbss.core.repository.zookeeper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ZookeeperHealth implements HealthIndicator {
	@Autowired
	private ZooKeeperFactory zooKeeperFactory;

	@Override
	public Health health() {
		Builder build = null;
		try {
			boolean is = zooKeeperFactory.isAlive();
			if (is) {
				build = Health.up();
				build.withDetail("info", String.format("state:%S,info:%S", is, zooKeeperFactory.info()));
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
