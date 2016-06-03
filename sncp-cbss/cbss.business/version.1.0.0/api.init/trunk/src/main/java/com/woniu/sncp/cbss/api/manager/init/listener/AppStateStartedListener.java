package com.woniu.sncp.cbss.api.manager.init.listener;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

import com.woniu.sncp.cbss.core.trace.aspect.listener.Trace;

public class AppStateStartedListener implements ApplicationListener<ApplicationStartedEvent> {

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		Trace.applicationState(event, event.getTimestamp(), event.getArgs(), event.getSpringApplication());
	}

}
