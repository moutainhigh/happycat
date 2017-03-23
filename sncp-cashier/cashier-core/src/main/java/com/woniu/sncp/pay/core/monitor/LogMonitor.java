package com.woniu.sncp.pay.core.monitor;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woniu.sncp.tools.ProxoolUtil;

/**
 * <p>descrption: 日志监控</p>
 * 
 * @author fuzl
 * @date   2016年10月8日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public class LogMonitor {

	
	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	public String monitoringFormat(String url, String method, String accessId,String accessType,long requestTime, 
			long startTime, long endTime,String retCode,String retMsg, String ip, String extMsg, boolean output) {
		StringBuilder log = new StringBuilder();
		log.append(extMsg);
		log.append("needLogToMonitorData:{\"uri\":\"");
		log.append(url);
		log.append("\",\"method\":\"");
		log.append(ObjectUtils.toString(method));
		log.append("\",\"accessId\":\"");
		log.append(accessId);
		log.append("\",\"accessType\":\"");
		log.append(accessType);
		log.append("\",\"port\":");
		log.append(ProxoolUtil.getTomcatPort());
		log.append(",\"clientIP\":\"");
		log.append(ip);
		log.append("\",\"requestTime\":");
		log.append(requestTime);
		log.append(",\"startTime\":");
		log.append(startTime);
		log.append(",\"retCode\":\"");
		log.append(retCode);
		log.append("\",\"retMsg\":\"");
		log.append(retMsg);
		log.append("\",\"endTime\":");
		log.append(endTime);
		log.append(",\"extMsg\":\"");
		log.append("");//extMsg放前缀
		log.append("\"}");

		if (output) {
			logger.info(log.toString());
		}

		return log.toString();
	}
}
