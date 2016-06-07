package cbss.api.manager.init.listener;

import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;

import com.woniu.sncp.cbss.core.trace.aspect.listener.Trace;

public class AppStatePreparedListener implements ApplicationListener<ApplicationPreparedEvent> {

	@Override
	public void onApplicationEvent(ApplicationPreparedEvent event) {
		Trace.applicationState(event, event.getTimestamp(), event.getArgs(), event.getSpringApplication());
	}

}
