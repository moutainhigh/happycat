package cbss.api.fcm.controller.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ApiControllerHealth implements HealthIndicator {
	
	@Override
	public Health health() {
		//TODO 具体需再实现
		return Health.unknown().build();
	}

}
