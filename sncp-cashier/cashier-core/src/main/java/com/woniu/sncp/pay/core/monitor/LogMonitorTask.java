package com.woniu.sncp.pay.core.monitor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * <p>descrption: </p>
 * 
 * @author fuzl
 * @date   2016年10月8日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public class LogMonitorTask implements Runnable{
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private LogMonitor logMonitor =  null;
	
	private String url;
	private String method;
	private String accessId;
	private String accessType;
	private Long requestTime;
	private Long startTime;
	private Long endTime;
	private String retCode;
	private String retMsg;
	private String ip;
	private String extMsg;
	private Boolean output;
	
	public LogMonitorTask(String url, String method, String accessId,String accessType,long requestTime, 
			long startTime, long endTime,String retCode,String retMsg, String ip, String extMsg, boolean output){
		this.url = url;
		this.method = method;
		this.accessId = accessId;
		this.accessType = accessType;
		this.requestTime = requestTime;
		this.startTime = startTime;
		this.endTime = endTime;
		this.retCode = retCode;
		this.retMsg = retMsg;
		this.ip = ip;
		this.extMsg = extMsg;
		this.output = output;
	}
	
	
	@Override
	public void run() {
		try{
			logMonitor =  new LogMonitor();
			logMonitor.monitoringFormat(url.toString(), method, accessId, accessType, requestTime, startTime, endTime, retCode, retMsg,ip, extMsg, true);
		} catch (Exception e){
			logger.error("log-monitor-exception " + e.getMessage(),e);
		}
	}


	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getAccessId() {
		return accessId;
	}

	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

	public String getAccessType() {
		return accessType;
	}

	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}

	public Long getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(Long requestTime) {
		this.requestTime = requestTime;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getEndTime() {
		return endTime;
	}

	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	public String getRetMsg() {
		return retMsg;
	}


	public void setRetMsg(String retMsg) {
		this.retMsg = retMsg;
	}

	public String getRetCode() {
		return retCode;
	}

	public void setRetCode(String retCode) {
		this.retCode = retCode;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getExtMsg() {
		return extMsg;
	}

	public void setExtMsg(String extMsg) {
		this.extMsg = extMsg;
	}

	public Boolean getOutput() {
		return output;
	}

	public void setOutput(Boolean output) {
		this.output = output;
	}
}

