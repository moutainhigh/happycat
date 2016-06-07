package com.woniu.sncp.cbss.core.trace.aspect.listener;

import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;

public class ServletContainerApplicationListener implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

	public static int port = -1;

	@Override
	public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
		port = event.getEmbeddedServletContainer().getPort();
	}

}
