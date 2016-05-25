package cbss.core.model.request.access;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import cbss.core.model.request.RequestDatas;
import cbss.core.util.IpUtils;

public class RequestAccess implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private RequestDatas requestDatas;

	private HttpServletRequest servletRequest;
	private String body;
	private String accessVerify;
	private String traceState;

	public String getRemoteIp() {
		return IpUtils.getRemoteAddr(servletRequest);
	}

	public HttpServletRequest getServletRequest() {
		return servletRequest;
	}

	public void setServletRequest(HttpServletRequest servletRequest) {
		this.servletRequest = servletRequest;
	}

	public String getTraceState() {
		return traceState;
	}

	public void setTraceState(String traceState) {
		this.traceState = traceState;
	}

	public String getAccessVerify() {
		return accessVerify;
	}

	public void setAccessVerify(String accessVerify) {
		this.accessVerify = accessVerify;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public RequestDatas getRequestDatas() {
		return requestDatas;
	}

	public void setRequestDatas(RequestDatas requestDatas) {
		this.requestDatas = requestDatas;
	}

	public Map getLimitData() {
		Map pkeys = getServletRequest().getParameterMap();
		Map datas = new HashMap(pkeys);
		Enumeration attrs = getServletRequest().getAttributeNames();
		while (attrs.hasMoreElements()) {
			String attr = String.valueOf(attrs.nextElement());
			if (!datas.containsKey(attr)) {
				datas.put(attr, getServletRequest().getAttribute(attr));
			}
		}
		Enumeration names = getServletRequest().getHeaderNames();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			datas.put(name, getServletRequest().getHeader(name));
		}

		return datas;
	}
}
