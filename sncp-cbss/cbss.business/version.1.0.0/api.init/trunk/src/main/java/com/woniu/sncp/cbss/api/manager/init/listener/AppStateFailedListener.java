package com.woniu.sncp.cbss.api.manager.init.listener;

import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;

import com.woniu.sncp.cbss.core.trace.aspect.listener.Trace;

public class AppStateFailedListener implements ApplicationListener<ApplicationFailedEvent> {

	@Override
	public void onApplicationEvent(ApplicationFailedEvent event) {
		Trace.applicationState(event, event.getTimestamp(), event.getArgs(), event.getSpringApplication());
	}

}
