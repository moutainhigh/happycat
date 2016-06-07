package cbss.api.manager.init.listener;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import com.woniu.sncp.cbss.core.trace.aspect.listener.Trace;

public class AppStateReadyListener implements ApplicationListener<ApplicationReadyEvent> {

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		Trace.applicationState(event, event.getTimestamp(), event.getArgs(), event.getSpringApplication());
	}

}
