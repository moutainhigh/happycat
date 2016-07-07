package com.woniu.sncp.cbss.api.profile.controller.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ApiControllerHealth implements HealthIndicator {
	
	
	public Health health() {
		return Health.unknown().build();
	}

}
