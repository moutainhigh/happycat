package com.woniu.sncp.cbss.core.trace.aspect.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ServletContainerApplicationListener implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

	public static int port = -1;

	@Autowired
	ApplicationContext applicationContext;

	@Override
	public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
		port = event.getEmbeddedServletContainer().getPort();
	}

}
