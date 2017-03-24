package com.woniu.sncp.pay.core.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import com.woniu.sncp.json.JsonUtils;
import com.woniu.sncp.pay.common.threadpool.ThreadPool;
import com.woniu.sncp.pay.core.monitor.LogMonitorTask;
import com.woniu.sncp.web.IpUtils;
import com.woniu.sncp.web.response.ResultResponse;

/**
 * 
 * <p>descrption: 监控日志过滤器</p>
 * 
 * @author fuzl
 * @date   2016年10月9日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public class LogMonitorFilter extends OncePerRequestFilter {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		Long requestTime = System.currentTimeMillis();
		Long startTime = null;
		Long endTime = null;
		StringBuffer url = new StringBuffer();
		String extMsg = "";
		String accessid="-1";
		String accesstype="1";
		try {
			
			Map<String, Object> requestParams = request.getParameterMap();
			Map<String, String> treeMap = putToTreeMap(request, requestParams);
			extMsg = treeMap.toString();
//			logRequestParams("(" + request.getMethod() + ")" + request.getRequestURL().toString(), request,treeMap);
			
			String accessId = request.getParameter("accessId");
			accessid = StringUtils.isEmpty(accessId)?accessid:accessId;
			
			String _accessId = request.getParameter("accessid");
			accessid = StringUtils.isEmpty(_accessId)?accessid:_accessId;
			
			String accessType = request.getParameter("accessType");
			accesstype = StringUtils.isEmpty(accessType)?accesstype:accessType;
			
			String _accessType = request.getParameter("accesstype");
			accesstype = StringUtils.isEmpty(_accessType)?accesstype:_accessType;
			
			startTime = System.currentTimeMillis();
			filterChain.doFilter(request, response);
			endTime = System.currentTimeMillis();
		} catch (Exception e) {
			writeJsonp(request.getParameter("callback"), response, new ResultResponse(ResultResponse.FAIL,"接口异常,"+e.getMessage(),null));
			return;
		} finally {
			
			url.append(request.getRequestURI());
			String method = request.getParameter("method");
			Object retCode = request.getAttribute("retCode");
			Object retMsg = request.getAttribute("retMsg");
			if(retCode == null){
				retCode = "";
			}
			ThreadPool.getInstance().executeTask(
					new LogMonitorTask(url.toString(), method, accessid,
							accesstype, requestTime, startTime, endTime,retCode==null?"":retCode.toString(), retMsg==null?"未获取到响应描述":retMsg.toString(),IpUtils.getRemoteAddr(request),extMsg, true));
			
		}
	}

	private Map<String, String> putToTreeMap(HttpServletRequest request,
			Map<String, Object> requestParams) {
		Map<String, String> treeMap = new TreeMap<String, String>();
		for (Iterator<Entry<String, Object>> keyValuePairs = requestParams
				.entrySet().iterator(); keyValuePairs.hasNext();) {
			Map.Entry<String, Object> entry = keyValuePairs.next();
			String key = entry.getKey();
			String value = request.getParameter(key);
			
			treeMap.put(key, value);
		}
		return treeMap;
	}
	
	protected void logRequestParams(String method, HttpServletRequest request,Map<String, String> treeMap) {
		try {
			StringBuffer sb = new StringBuffer();
			Iterator<String> iter = treeMap.keySet().iterator();
			sb.append("\n++++++[" + method + "]参数 开始++++++\n");
			sb.append("requestIp=" + IpUtils.getRemoteAddr(request));
			sb.append("\n");
			while (iter.hasNext()) {
				String name = (String) iter.next();
				sb.append(name + "=" + treeMap.get(name));
				sb.append("\n");
			}
			sb.append("++++++[" + method + "]参数 结束++++++");
			logger.info(sb.toString());
		} catch (Exception e1) {
			logger.error("获取请求[" + method + "]参数异常", e1);
		}
	}
	
	/**
	 * 带callback参数时，返回jsonp格式，否则返回json格式
	 * 
	 * @param callback
	 * @param response
	 * @param resultMap
	 */
	protected void writeJsonp(String callback, HttpServletResponse response, Object result) {

		PrintWriter out;
		try {
			out = response.getWriter();
			if (StringUtils.isEmpty(callback)) {
				out.print(JsonUtils.toJson(result));
			} else {
				response.setContentType("text/javascript; charset=UTF-8");
				out.print(callback + "(" + JsonUtils.toJson(result) + ")");
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
