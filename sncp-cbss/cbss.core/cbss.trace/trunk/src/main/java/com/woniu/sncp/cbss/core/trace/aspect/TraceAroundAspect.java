package com.woniu.sncp.cbss.core.trace.aspect;

import java.util.Calendar;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.woniu.sncp.cbss.core.trace.aspect.listener.TraceAroundEvent;
import com.woniu.sncp.cbss.core.trace.model.TraceMethodAround;

@Aspect
@Component
public class TraceAroundAspect {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private CounterService counterService;

	private Object aroundIn(ProceedingJoinPoint joinPoint)
			throws Throwable {
		TraceMethodAround aroundEvent = null;
		Object outData = null;
		try {
			try {
				aroundEvent = new TraceMethodAround(joinPoint);
				aroundEvent.setMethodStartTime(Calendar.getInstance().getTime());
				aroundEvent.setMethodInData(joinPoint.getArgs());
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 方法执行
			outData = joinPoint.proceed();

		} finally {
			try {
				if (aroundEvent != null) {
					aroundEvent.setMethodExecEndTime(Calendar.getInstance().getTime());
					aroundEvent.setMethodOutData(outData);
					applicationContext.publishEvent(new TraceAroundEvent(aroundEvent));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return outData;

	}

	@Around("execution(* cbss.api..*.*(..))")
	public Object aroundApi(ProceedingJoinPoint joinPoint)
			throws Throwable {
		return aroundIn(joinPoint);
	}

	@Around("execution(* com.woniu.sncp..*Impl.*(..))")
	public Object aroundImpl(ProceedingJoinPoint joinPoint)
			throws Throwable {
		return aroundIn(joinPoint);
	}

}
