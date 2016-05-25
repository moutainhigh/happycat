package cbss.core.trace.aspect;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cbss.core.trace.LogFormat;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 切面类 <br />
 * 功能1：通过注解{@link ParamsAndReturningLog}实现输入参数和输出参数的打印<br />
 * 
 * @author yanghao 2010-4-1
 * 
 */
@Aspect
@Component
public class ParamsLoggerAspectj {

	@Autowired
	private LogFormat log4jFormat;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 可直接写pointcut，也可用方法标识pointcut<br />
	 * 例如：@annotation(paramsAndReturningLog)
	 * 
	 * @param joinPoint
	 */
	@Before("@annotation(paramsAndReturningLog)")
	public void loggerInputParams(JoinPoint joinPoint, ParamsAndReturningLog paramsAndReturningLog) {
		boolean isLog = paramsAndReturningLog != null ? paramsAndReturningLog.isLog() : true;
		try {
			if (isLog && logger.isInfoEnabled()) {
				String log4j = log4jFormat.format("AOP入参", joinPoint.getTarget().getClass().getSimpleName(), joinPoint.getSignature().getName(),
						ArrayUtils.isEmpty(joinPoint.getArgs()) ? null : JSONArray.toJSONString(joinPoint.getArgs()), null, null, null, null, null, false);
				logger.info(log4j);
			}
		} catch (Exception e) {
			String log4j = log4jFormat.format("AOP入参异常", null, null, null, null, null, null, null, "AOP打印开始参数异常" + e.getMessage(), false);
			logger.info(log4j);
		}
	}

	@AfterReturning(pointcut = "@annotation(paramsAndReturningLog)", returning = "retValue")
	public void loggerOutputParams(JoinPoint joinPoint, Object retValue, ParamsAndReturningLog paramsAndReturningLog) {
		boolean isLog = paramsAndReturningLog != null ? paramsAndReturningLog.isLog() : true;

		try {
			if (isLog && logger.isInfoEnabled()) {
				String log4j = log4jFormat.format("AOP返回", joinPoint.getTarget().getClass().getSimpleName(), joinPoint.getSignature().getName(),
						JSONObject.toJSONString(retValue), null, null, null, null, null, false);
				logger.info(log4j);
			}
		} catch (Exception e) {
			String log4j = log4jFormat.format("AOP返回异常", null, null, null, null, null, null, null, "AOP返回异常" + e.getMessage(), false);
			logger.info(log4j);
		}
	}

	@Around("@annotation(paramsAndReturningLog)")
	public Object logAround(ProceedingJoinPoint joinPoint, ParamsAndReturningLog paramsAndReturningLog) throws Throwable {
		boolean isLog = paramsAndReturningLog != null ? paramsAndReturningLog.isLog() : true;
		long time = System.currentTimeMillis();
		Object retValue = null;
		try {
			retValue = joinPoint.proceed();
			return retValue; // continue on the intercepted method
		}catch(Throwable e){
			retValue = e.getMessage();
			throw e;
		} finally {
			String content = (ArrayUtils.isEmpty(joinPoint.getArgs()) ? null : JSONArray.toJSONString(joinPoint.getArgs())) + "|||" + JSONObject.toJSONString(retValue);
			if (isLog && logger.isInfoEnabled()) {
				String log4j = log4jFormat.format("AOP执行耗时", joinPoint.getTarget().getClass().getSimpleName(), joinPoint.getSignature().getName(),
						content,
						ObjectUtils.toString(time), ObjectUtils.toString(System.currentTimeMillis() - time), null, null, null, false);
				logger.info(log4j);
			}
		}
	}
}
