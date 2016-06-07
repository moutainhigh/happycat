package com.woniu.sncp.cbss.core.model.request.access;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.woniu.sncp.cbss.core.model.request.RequestDatas;

public class RequestAccess implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Date reciveTime = new Date();

	private RequestDatas requestDatas;

	private String body;
	private String accessVerify;
	private String traceState;

	private String remoteIp;
	private Map requestParamData;
	private String requestURI;
	private String sessionId;
	public Date getReciveTime() {
		return reciveTime;
	}
	public void setReciveTime(Date reciveTime) {
		this.reciveTime = reciveTime;
	}
	public RequestDatas getRequestDatas() {
		return requestDatas;
	}
	public void setRequestDatas(RequestDatas requestDatas) {
		this.requestDatas = requestDatas;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getAccessVerify() {
		return accessVerify;
	}
	public void setAccessVerify(String accessVerify) {
		this.accessVerify = accessVerify;
	}
	public String getTraceState() {
		return traceState;
	}
	public void setTraceState(String traceState) {
		this.traceState = traceState;
	}
	public String getRemoteIp() {
		return remoteIp;
	}
	public void setRemoteIp(String remoteIp) {
		this.remoteIp = remoteIp;
	}
	public Map getRequestParamData() {
		return requestParamData;
	}
	public void setRequestParamData(Map requestParamData) {
		this.requestParamData = requestParamData;
	}
	public String getRequestURI() {
		return requestURI;
	}
	public void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

}
