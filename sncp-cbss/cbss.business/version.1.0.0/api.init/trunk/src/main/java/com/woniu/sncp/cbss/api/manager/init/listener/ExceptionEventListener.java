package com.woniu.sncp.cbss.api.manager.init.listener;

import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;

public class ExceptionEventListener implements ApplicationListener<ApplicationFailedEvent> {

	@Override
	public void onApplicationEvent(ApplicationFailedEvent event) {
		Throwable exception = event.getException();
		handleException(exception);
	}

	private void handleException(Throwable exception) {
		
	}

}
