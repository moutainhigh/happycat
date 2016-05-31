package cbss.core.trace.aspect;

import java.util.Calendar;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import cbss.core.trace.aspect.listener.TraceExceptionEvent;
import cbss.core.trace.model.TraceMethodException;

@Aspect
@Component
public class TraceCounterExceptionAspect {

	@Autowired
	private ApplicationContext applicationContext;

	@AfterThrowing(pointcut = "execution(* cbss.api..*.*(..)) throws Exception", throwing = "ex")
	public void exceptionApi(JoinPoint joinPoint, Throwable ex) {
		afterThrowing(joinPoint, ex);
	}

	@AfterThrowing(pointcut = "execution(* com.woniu.sncp..*Impl.*(..)) throws Exception", throwing = "ex")
	public void exceptionImpl(JoinPoint joinPoint, Throwable ex) {
		afterThrowing(joinPoint, ex);
	}

	private void afterThrowing(JoinPoint joinPoint, Throwable ex) {
		try {
			TraceMethodException traceMethodException = new TraceMethodException(joinPoint);
			traceMethodException.setMethodStartTime(Calendar.getInstance().getTime());
			traceMethodException.setMethodExecEndTime(Calendar.getInstance().getTime());
			traceMethodException.setMethodOutData(joinPoint.getArgs());
			traceMethodException.setMethodOutException(ex);
			applicationContext.publishEvent(new TraceExceptionEvent(traceMethodException));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
