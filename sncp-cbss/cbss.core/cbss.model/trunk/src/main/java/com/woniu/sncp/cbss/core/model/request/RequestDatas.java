package com.woniu.sncp.cbss.core.model.request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.woniu.sncp.cbss.core.model.access.AccessSecurityInfo;
import com.woniu.sncp.cbss.core.model.access.SecurityResource;

/**
 * 请求对象
 *
 */
public class RequestDatas<T extends RequestParam> implements Serializable {
	/*
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*
	 * 版本
	 */
	private String version;
	/*
	 * 客户端请求信息
	 */
	private List<RequestClientInfo> clientInfo = new ArrayList<RequestClientInfo>(0);
	/*
	 * 接口权限accessId
	 */
	private Long accessId;
	/*
	 * 接口权限accessType
	 */
	private Long accessType;
	/*
	 * 接口权限accessPasswd
	 */
	private String accessPasswd;
	/*
	 * 接口权限serviceShortName 业务简称
	 */
	private String serviceShortName;
	/*
	 * 单条数据
	 */
	private T paramdata;
	/*
	 * 数据是数组形势
	 */
	private List<T> paramdatas;
	/*
	 * 请求扩展数据
	 */
	private RequestOther other = new RequestOther();

	private String traceState;
	private AccessSecurityInfo accessSecurityInfo;
	private SecurityResource securityResource;
	private String sessionId;

	private Long reciveTime = null;
	private Long accessAuthorizeEndtime = null;
	private String remoteIp;

	public String getRemoteIp() {
		return remoteIp;
	}

	public void setRemoteIp(String remoteIp) {
		this.remoteIp = remoteIp;
	}

	public Long getAccessAuthorizeEndtime() {
		return accessAuthorizeEndtime;
	}

	public void setAccessAuthorizeEndtime(Long accessAuthorizeEndtime) {
		this.accessAuthorizeEndtime = accessAuthorizeEndtime;
	}

	public Long getReciveTime() {
		return reciveTime;
	}

	public void setReciveTime(Long reciveTime) {
		this.reciveTime = reciveTime;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<RequestClientInfo> getClientInfo() {
		return clientInfo;
	}

	public void setClientInfo(List<RequestClientInfo> clientInfo) {
		this.clientInfo = clientInfo;
	}

	public void setClientInfo(RequestClientInfo clientInfo) {
		this.clientInfo = new ArrayList<RequestClientInfo>(1);
		this.clientInfo.add(clientInfo);
	}

	public Long getAccessId() {
		return accessId;
	}

	public void setAccessId(Long accessId) {
		this.accessId = accessId;
	}

	public Long getAccessType() {
		return accessType;
	}

	public void setAccessType(Long accessType) {
		this.accessType = accessType;
	}

	public String getAccessPasswd() {
		return accessPasswd;
	}

	public void setAccessPasswd(String accessPasswd) {
		this.accessPasswd = accessPasswd;
	}

	public String getServiceShortName() {
		return serviceShortName;
	}

	public void setServiceShortName(String serviceShortName) {
		this.serviceShortName = serviceShortName;
	}

	public T getParamdata() {
		return paramdata;
	}

	public void setParamdata(T paramdata) {
		this.paramdata = paramdata;
	}

	public List<T> getParamdatas() {
		return paramdatas;
	}

	public void setParamdatas(List<T> paramdatas) {
		this.paramdatas = paramdatas;
	}

	public RequestOther getOther() {
		return other;
	}

	public void setOther(RequestOther other) {
		this.other = other;
	}

	public String getTraceState() {
		return traceState;
	}

	public void setTraceState(String traceState) {
		this.traceState = traceState;
	}

	public AccessSecurityInfo getAccessSecurityInfo() {
		return accessSecurityInfo;
	}

	public void setAccessSecurityInfo(AccessSecurityInfo accessSecurityInfo) {
		this.accessSecurityInfo = accessSecurityInfo;
	}

	public SecurityResource getSecurityResource() {
		return securityResource;
	}

	public void setSecurityResource(SecurityResource securityResource) {
		this.securityResource = securityResource;
	}

}
