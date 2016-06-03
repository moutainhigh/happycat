package cbss.core.trace.aspect.listener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.stereotype.Component;

import cbss.core.util.DateUtils;
import cbss.core.util.IpUtils;

import com.alibaba.fastjson.JSONObject;

@Component
public class Trace {

	private final static Logger logger = LoggerFactory.getLogger(Trace.class);

	public static void applicationState(SpringApplicationEvent type, long timestamp, String[] args, SpringApplication springApplication) {
		Map<String, Object> infos = new HashMap<String, Object>();
		infos.put("AppProcessState", type.getClass().getName());

		Map<String, Object> ds = new HashMap<String, Object>();
		ds.put("ip", IpUtils.getLoaclAddr());
		ds.put("time", timestamp);
		ds.put("args", args);
		ds.put("mainclass", springApplication.getMainApplicationClass().getName());

		infos.put("AppProcessInfos", ds);

		logger.trace(JSONObject.toJSONString(infos));
	}

	public String traceException(String exception, String traceState, String traceMsg) {
		if ("1".equals(traceState)) {
			exception = exception + "|[Dg]" + traceMsg;
		}
		return exception;
	}

	public void trace(Map<String, Object> tracelog) {
		tracelog.put("meminfo", new Long[] { Runtime.getRuntime().freeMemory(), Runtime.getRuntime().maxMemory(), Runtime.getRuntime().totalMemory() });
		logger.trace(JSONObject.toJSONString(tracelog));
	}

	public String traceApiTime(String url, Object paramData, Long requestTime, Date receiveTime, Date finishTime, Date accessAuthorizeEndtime) {
		Map<String, Object> traceTime = new HashMap<String, Object>();
		Map<String, Object> urltimeinfos = new HashMap<String, Object>();
		urltimeinfos.put("url", url);
		urltimeinfos.put("paramData", JSONObject.toJSONString(paramData));

		if (requestTime != null) {
			traceTime.put(url, finishTime.getTime() - requestTime);
			urltimeinfos.put("requestTime", DateUtils.format(new Date(requestTime), DateUtils.TIMESTAMP_MS));
			urltimeinfos.put("rcrqTime", receiveTime.getTime() - requestTime);
		}
		urltimeinfos.put("receiveTime", DateUtils.format(receiveTime, DateUtils.TIMESTAMP_MS));
		urltimeinfos.put("authTime", DateUtils.format(accessAuthorizeEndtime, DateUtils.TIMESTAMP_MS));
		urltimeinfos.put("finishTime", DateUtils.format(finishTime, DateUtils.TIMESTAMP_MS));
		urltimeinfos.put("aurcTime", accessAuthorizeEndtime.getTime() - receiveTime.getTime());
		urltimeinfos.put("fiauTime", finishTime.getTime() - accessAuthorizeEndtime.getTime());
		urltimeinfos.put("fircTime", finishTime.getTime() - receiveTime.getTime());

		traceTime.put(url + "#infos", urltimeinfos);

		String info = JSONObject.toJSONString(traceTime);
		logger.trace(info);
		return info;
	}
}
