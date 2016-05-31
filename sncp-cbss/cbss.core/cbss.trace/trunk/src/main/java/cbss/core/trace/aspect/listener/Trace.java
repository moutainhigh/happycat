package cbss.core.trace.aspect.listener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cbss.core.util.DateUtils;

import com.alibaba.fastjson.JSONObject;

@Component
public class Trace {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public String traceException(String exception, String traceState, String traceMsg) {
		if ("1".equals(traceState)) {
			exception = exception + "|[Dg]" + traceMsg;
		}
		return exception;
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