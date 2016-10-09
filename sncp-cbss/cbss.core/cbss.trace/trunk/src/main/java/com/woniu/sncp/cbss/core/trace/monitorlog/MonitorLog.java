package com.woniu.sncp.cbss.core.trace.monitorlog;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.woniu.sncp.cbss.core.util.ThreadPool;

@Component
public class MonitorLog {

	/**
	 * 异步记录Trace日志
	 * 
	 */
	class AyncTraceLog implements Runnable {
		private Object loginfo;

		public AyncTraceLog(Object loginfo) {
			this.loginfo = loginfo;
		}

		@Override
		public void run() {
			logger.trace(JSONObject.toJSONString(loginfo));
		}
	}

	private final static Logger logger = LoggerFactory.getLogger(MonitorLog.class);

	@Autowired
	private ThreadPool threadPool;

	/**
	 * @param uri
	 * @param method
	 * @param accessid
	 * @param accessType
	 * @param port
	 * @param clientIp
	 * @param requestTime
	 * @param startTime
	 * @param retCode
	 * @param endTime
	 * @param exts
	 */
	public void write(String uri, String method, String accessid, String accessType, int port, String clientIp, long requestTime, long startTime, String retCode, long endTime, Object exts) {
		MonitorLogRecord monitorLogRecord = new MonitorLogRecord();
		monitorLogRecord.setUri(uri);
		monitorLogRecord.setMethod(method);
		monitorLogRecord.setAccessid(accessid);
		monitorLogRecord.setAccessType(accessType);
		monitorLogRecord.setPort(port);
		monitorLogRecord.setClientIp(clientIp);
		monitorLogRecord.setRequestTime(requestTime);
		monitorLogRecord.setStartTime(startTime);
		monitorLogRecord.setRetCode(retCode);
		monitorLogRecord.setEndTime(endTime);
		monitorLogRecord.setExtMsg("info="+JSON.toJSONString(exts));

		threadPool.executeTask(new AyncTraceLog(monitorLogRecord));
	}

	private String extMsg(Map<String, Object> exts) {
		StringBuffer buffer = new StringBuffer();
		Set<String> keys = exts.keySet();
		for (String key : keys) {
			buffer.append(key).append("=").append(JSON.toJSONString(exts.get(key))).append(",");
		}
		return buffer.toString();
	}
}
