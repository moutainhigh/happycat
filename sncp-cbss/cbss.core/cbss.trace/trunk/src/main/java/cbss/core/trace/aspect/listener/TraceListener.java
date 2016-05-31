package cbss.core.trace.aspect.listener;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import cbss.core.trace.model.TraceMethodAround;
import cbss.core.trace.model.TraceMethodException;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

@Component
@Async("threadPoolTaskScheduler")
public class TraceListener {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GaugeService gaugeService;

	@Autowired
	private CounterService counterService;

	@Autowired
	private TraceConfigurationProperties traceConfigurationProperties;

	@EventListener
	public void processBlackListEvent(TraceEvent event) {
		if (event instanceof TraceBeforeEvent) {

		} else if (event instanceof TraceAfterEvent) {

		} else if (event instanceof TraceExceptionEvent) {
			Object source = ((TraceExceptionEvent) event).getSource();
			if (source instanceof TraceMethodException) {

				TraceMethodException src = ((TraceMethodException) source);

				Object[] args1 = (Object[]) src.getMethodInData();
				src.setMethodExecSkipTime(((Date) src.getMethodExecEndTime()).getTime() - ((Date) src.getMethodStartTime()).getTime());
				src.setMethodInData(args1 == null ? "" : JSONObject.toJSONString(args1));
				src.setMethodOutData(JSONObject.toJSONString(src.getMethodOutData()));
				src.setRecordUUID(JSONObject.toJSONString(args1 == null ? "" : args1));
				Throwable throwable = (Throwable) src.getMethodOutException();
				StringBuffer ex = null;
				if (throwable != null) {
					if (throwable.getMessage() != null) {
						ex = new StringBuffer(throwable.getMessage());
					} else {
						ex = new StringBuffer();
					}
					ex.append("#");
					StackTraceElement[] stacktrace = throwable.getStackTrace();
					if (stacktrace != null) {
						int i = 0;
						for (StackTraceElement traceElement : stacktrace) {
							i++;
							ex.append(traceElement.getMethodName()).append("#").append(traceElement.getLineNumber()).append("#").append(traceElement.getFileName()).append("#")
									.append(traceElement.toString()).append("\n");
							if (i >= traceConfigurationProperties.getExceptionStackTraceDeepLength()) {
								break;
							}
						}
					}
				}
				
				if (ex != null) {
					src.setMethodOutException(ex.toString());
					logger.trace(JSONObject.toJSONString(src, SerializerFeature.WriteDateUseDateFormat));
					counterService.increment(ex.getClass().getName() + "#" + src.getClassPathMethodName());
				} else {
					logger.trace(JSONObject.toJSONString(src, SerializerFeature.WriteDateUseDateFormat));
					counterService.increment("Null Exception Message#" + src.getClassPathMethodName());
				}
			}
		} else if (event instanceof TraceAroundEvent) {
			Object source = ((TraceAroundEvent) event).getSource();
			if (source instanceof TraceMethodAround) {

				TraceMethodAround src = ((TraceMethodAround) source);

				Object[] args1 = (Object[]) src.getMethodInData();

				src.setMethodExecSkipTime(((Date) src.getMethodExecEndTime()).getTime() - ((Date) src.getMethodStartTime()).getTime());
				boolean isMethodIndata = true;
				for (Object args : args1) {
					if (args.getClass().getName().equals("cbss.core.authorize.AccessAuthorizeRequestWrapper")) {
						isMethodIndata = false;
					}
				}
				if (isMethodIndata) {
					src.setMethodInData(JSONObject.toJSONString(args1 == null ? new Object() : args1));
					src.setRecordUUID(JSONObject.toJSONString(args1 == null ? new Object() : args1));
				} else {
					src.setMethodInData("");
					src.setRecordUUID(JSONObject.toJSONString(new Object()));
				}
				src.setMethodOutData(JSONObject.toJSONString(src.getMethodOutData()));

				logger.trace(JSONObject.toJSONString(src, SerializerFeature.WriteDateUseDateFormat));

				gaugeService.submit(src.getClassPathMethodName(), src.getMethodExecSkipTime());
			}
		}
	}
}
