package com.woniu.sncp.cbss.core.trace.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CounterBeforeAspect {

	@Autowired
	private CounterService counterService;

	@Before("execution(* com.woniu.sncp.cbss.api..*.*(..))")
	public void increment(JoinPoint joinPoint) {
		counterService.increment(joinPoint.getSignature().getDeclaringTypeName() + "#" + joinPoint.getSignature().getName());
	}
}
