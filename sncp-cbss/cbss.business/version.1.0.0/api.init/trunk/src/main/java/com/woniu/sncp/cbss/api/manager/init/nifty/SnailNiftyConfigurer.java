package com.woniu.sncp.cbss.api.manager.init.nifty;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bigdullrock.spring.boot.nifty.autoconfigure.NiftyAutoConfiguration.NiftyConfigurer;
import com.woniu.sncp.cbss.core.authorize.nifty.SnailNiftyMethodInterceptor;

@Component
public class SnailNiftyConfigurer implements NiftyConfigurer {

	@Autowired
	public SnailNiftyMethodInterceptor snailNiftyMethodInterceptor;

	@Override
	public void configureProxyFactory(ProxyFactory proxyFactory) {
		proxyFactory.setOptimize(true);
		proxyFactory.addAdvice(snailNiftyMethodInterceptor);
	}
}
