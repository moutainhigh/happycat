package cbss.core.model.request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cbss.core.model.access.AccessSecurityInfo;
import cbss.core.model.access.SecurityResource;

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
	public String version;
	/*
	 * 客户端请求信息
	 */
	public List<RequestClientInfo> clientInfo = new ArrayList<RequestClientInfo>(0);
	/*
	 * 接口权限accessId
	 */
	public Long accessId;
	/*
	 * 接口权限accessType
	 */
	public Long accessType;
	/*
	 * 接口权限accessPasswd
	 */
	public String accessPasswd;
	/*
	 * 接口权限serviceShortName 业务简称
	 */
	public String serviceShortName;
	/*
	 * 单条数据
	 */
	public T paramdata;
	/*
	 * 数据是数组形势
	 */
	public List<T> paramdatas;
	/*
	 * 请求扩展数据
	 */
	public RequestOther other = new RequestOther();

	private String traceState;
	private AccessSecurityInfo accessSecurityInfo;
	private SecurityResource securityResource;
	private String sessionId;
	
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
