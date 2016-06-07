package com.woniu.sncp.cbss.core.authorize;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.woniu.sncp.cbss.core.authorize.exception.AccessAuthorizeException;
import com.woniu.sncp.cbss.core.authorize.exception.AccessLimitException;
import com.woniu.sncp.cbss.core.errorcode.EchoInfo;
import com.woniu.sncp.cbss.core.errorcode.ErrorCode;
import com.woniu.sncp.cbss.core.model.constant.NameFactory;
import com.woniu.sncp.cbss.core.model.request.Param;
import com.woniu.sncp.cbss.core.model.request.ParamValueValidateException;
import com.woniu.sncp.cbss.core.model.request.RequestClientInfo;
import com.woniu.sncp.cbss.core.model.request.RequestDatas;
import com.woniu.sncp.cbss.core.model.request.RequestParam;
import com.woniu.sncp.cbss.core.model.request.access.RequestAccess;
import com.woniu.sncp.cbss.core.trace.aspect.listener.Trace;
import com.woniu.sncp.cbss.core.util.IpUtils;

/**
 * 权限、频率、参数过滤器
 * 
 * <pre>
 *  1.封装请求对象
 *  2.封装响应对象
 *  3.校验请求头信息
 *  4.转换业务参数
 *  4.1 业务参数值校验
 *  5.封装校验数据
 *  6.访问权限认证
 *  7.访问频率限制
 *  8.重新封装业务数据将验证资源数据存入
 *  9.下一个过滤链
 * 
 * <pre>
 * @author Administrator
 *
 */
@Component
public class AccessAuthorizeFilter implements Filter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private AccessAuthorize accessAuthorize;

	@Autowired
	private AccessUrlConfigurationProperties accessUrlConfigurationProperties;

	@Autowired
	private ErrorCode errorCode;

	@Autowired
	private Trace trace;

	@Autowired
	private IpUtils ipUtils;

	@Override
	public void destroy() {
	}

	private void echoInfo(ServletResponse servletResponse, EchoInfo<Object> echoInfo)
			throws IOException {
		servletResponse.setContentType("application/json;charset=UTF-8");
		servletResponse.getWriter().print(JSONObject.toJSONString(echoInfo));
		return;
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		RequestAccess requestAccess = new RequestAccess();
		requestAccess.setSessionId(UUID.randomUUID().toString());

		// 1.封装请求对象
		AccessAuthorizeRequestWrapper accessAuthorizeRequestWrapper = new AccessAuthorizeRequestWrapper((HttpServletRequest) servletRequest);
		// 2.封装响应对象
		HttpServletResponseWrapper httpServletResponseWrapper = new HttpServletResponseWrapper((HttpServletResponse) servletResponse);

		Date accessAuthorizeEndtime = null;
		try {
			try {

				// 初始http部分数据
				requestAccess.setRequestParamData(buildLimitParamData(accessAuthorizeRequestWrapper));
				requestAccess.setRequestURI(accessAuthorizeRequestWrapper.getRequestURI());
				requestAccess.setRemoteIp(ipUtils.getRemoteAddr(accessAuthorizeRequestWrapper));
				requestAccess.setBody(accessAuthorizeRequestWrapper.getBody());

				// 3.校验请求头信息
				String headAccessverify = accessAuthorizeRequestWrapper.getHeader(NameFactory.request_head.accessverify.name());
				String headAccessId = accessAuthorizeRequestWrapper.getHeader(NameFactory.request_head.accessId.name());
				String headAccessType = accessAuthorizeRequestWrapper.getHeader(NameFactory.request_head.accessType.name());
				String headAccessPasswd = accessAuthorizeRequestWrapper.getHeader(NameFactory.request_head.accessPasswd.name());

				if (StringUtils.isBlank(headAccessverify)) {
					echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1, requestAccess.getSessionId()).setData("HTTP HEADER accessverify IS MUST SET.")));
					return;
				}
				if (StringUtils.isBlank(headAccessId)) {
					echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1, requestAccess.getSessionId()).setData("HTTP HEADER accessId IS MUST SET.")));
					return;
				}
				if (StringUtils.isBlank(headAccessType)) {
					echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1, requestAccess.getSessionId()).setData("HTTP HEADER accessType IS MUST SET.")));
					return;
				}
				if (StringUtils.isBlank(headAccessPasswd)) {
					echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1, requestAccess.getSessionId()).setData("HTTP HEADER accessPasswd IS MUST SET.")));
					return;
				}

				// 初始http部分数据
				requestAccess.setAccessVerify(headAccessverify);

				// 4.转换业务参数
				int index = accessUrlConfigurationProperties.getUrls().indexOf(accessAuthorizeRequestWrapper.getRequestURI());
				if (accessUrlConfigurationProperties.getParamTypes() == null) {
					echoInfo(httpServletResponseWrapper,
							(errorCode.getErrorCode(-1, requestAccess.getSessionId()).setData("NOT FOUND PARAM-OBJECT URI:" + accessAuthorizeRequestWrapper.getRequestURI())));
					return;
				}
				RequestDatas<RequestParam> paramType = accessUrlConfigurationProperties.getParamObjects().get(index);
				if (null == paramType) {
					echoInfo(httpServletResponseWrapper,
							(errorCode.getErrorCode(-1, requestAccess.getSessionId()).setData("NOT FOUND PARAM-OBJECT URI:" + accessAuthorizeRequestWrapper.getRequestURI())));
					return;
				}

				RequestDatas<?> requestDatas = JSONObject.parseObject(accessAuthorizeRequestWrapper.getBody(), paramType.getClass());

				// 4.1 业务参数值校验
				requestDatas.setAccessId(Long.parseLong(headAccessId));
				requestDatas.setAccessType(Long.parseLong(headAccessType));
				requestDatas.setAccessPasswd(headAccessPasswd);

				RequestParam paramdata = requestDatas.getParamdata();
				if (paramdata != null) {
					if (paramdata instanceof Param) {
						try {
							if (!paramdata.checkParamValueIn()) {
								echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1, requestAccess.getSessionId()).setData("HTTP PARAM CHECK ERROR")));
								return;
							}
						} catch (ParamValueValidateException e) {
							echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1, requestAccess.getSessionId()).setData("HTTP PARAM CHECK ERROR").setErrorInfo(e.getMessage())));
							return;
						}
					}
				}

				List<?> paramdatas = requestDatas.getParamdatas();
				if (paramdatas != null) {
					for (Object object : paramdatas) {
						if (object instanceof Param) {
							try {
								if (!paramdata.checkParamValueIn()) {
									echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1, requestAccess.getSessionId()).setData("HTTP HEADER ACCESSVERIFY IS MUST SET.")));
									return;
								}
							} catch (ParamValueValidateException e) {
								echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1, requestAccess.getSessionId()).setData("HTTP PARAM CHECK ERROR.").setErrorInfo(e.getMessage())));
								return;
							}
						}
					}
				}
				// 4.2 客户端请求参数请求时间参数校验
				List<RequestClientInfo> clientInfos = requestDatas.getClientInfo();
				for (RequestClientInfo clientInfo : clientInfos) {
					try {
						if (clientInfo.getStartReqTime() <= 0) {
							echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1, requestAccess.getSessionId()).setData("HTTP PARAM RequestClientInfo's startReqTime Value is MUST SET.")));
							return;
						}
					} catch (Exception e) {
						echoInfo(httpServletResponseWrapper,
								(errorCode.getErrorCode(-1, requestAccess.getSessionId()).setData("HTTP PARAM RequestClientInfo's startReqTime Value is MUST SET.").setErrorInfo(e.getMessage())));
						return;
					}
				}

				// 4.3
				if (null != requestDatas.getAccessSecurityInfo()) {
					echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1, requestAccess.getSessionId()).setData("HTTP PARAM TOO MORE.1")));
					return;
				}
				if (null != requestDatas.getSecurityResource()) {
					echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1, requestAccess.getSessionId()).setData("HTTP PARAM TOO MORE.2")));
					return;
				}

				// 4.4
				requestAccess.setRequestDatas(requestDatas);
				requestDatas.setSessionId(requestAccess.getSessionId());

				// 5.封装校验数据
				if (requestAccess.getRequestDatas().getOther() != null && requestAccess.getRequestDatas().getOther().getOtherFirst() != null) {
					String traceState = ObjectUtils.toString((requestAccess.getRequestDatas().getOther().getOtherFirst().get(NameFactory.request_otherinfo.traceState.name())));
					requestDatas.setTraceState(traceState);
					requestAccess.setTraceState(traceState);
				}

				// 6.访问权限认证
				try {
					if (!accessAuthorize.authorize(requestAccess)) {
						echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1, requestDatas.getSessionId()).setData("REQUEST AUTHORIZE FOBIDDEN.")));
						return;
					}
				} catch (AccessAuthorizeException e) {
					echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1, requestDatas.getSessionId()).setData("REQUEST AUTHORIZE FOBIDDEN.").setErrorInfo(e.getMessage())));
					return;
				}

				// 7.访问频率限制
				try {
					if (accessAuthorize.limit(requestAccess)) {
						echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1, requestDatas.getSessionId()).setData("REQUEST ACCESS FREQUENCY TOO MORE.")));
						return;
					}
				} catch (AccessLimitException e) {
					echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1, requestDatas.getSessionId()).setData("REQUEST ACCESS FREQUENCY TOO MORE.").setErrorInfo(e.getMessage())));
					return;
				}

				if (requestAccess.getRequestDatas().getSecurityResource() != null
						&& requestAccess.getRequestDatas().getSecurityResource().getNoteFirst().containsKey(NameFactory.default_constant.ISSETSECURITYRESOURCES.getValue())) {
					// 8.重新封装业务数据将验证资源数据存入
				} else {
					requestAccess.getRequestDatas().setAccessSecurityInfo(null);
					requestAccess.getRequestDatas().setSecurityResource(null);
				}

				accessAuthorizeRequestWrapper.setBody(JSONObject.toJSONString(requestAccess.getRequestDatas()));

			} finally {
				accessAuthorizeEndtime = new Date();
			}
			// 9.下一个过滤链
			filterChain.doFilter(accessAuthorizeRequestWrapper, httpServletResponseWrapper);

		} catch (Exception e) {
			logger.error("doFilter", e);
			echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1, requestAccess.getSessionId()).setData("HTTP REQUEST ERROR").setErrorInfo(e.getMessage())));
			return;
		} finally {
			try {
				if (requestAccess != null && requestAccess.getRequestDatas() != null && requestAccess.getRequestDatas().getClientInfo() != null) {
					// url,入参,请求时间,接到时间,接到前网络消耗时间,处理结束时间,接到到处理之间的时间
					List<RequestClientInfo> clinfos = requestAccess.getRequestDatas().getClientInfo();
					for (RequestClientInfo requestClientInfo : clinfos) {
						trace.traceApiTime(accessAuthorizeRequestWrapper.getRequestURI(), requestAccess, requestClientInfo.getStartReqTime(), requestAccess.getReciveTime(), new Date(),
								accessAuthorizeEndtime);
					}
				} else {
					trace.traceApiTime(accessAuthorizeRequestWrapper.getRequestURI(), requestAccess, null, requestAccess.getReciveTime(), new Date(), accessAuthorizeEndtime);
				}
			} catch (Exception e) {
				logger.error("traceApiTime", e);
			}
			requestAccess = null;
			accessAuthorizeRequestWrapper.clear();
		}
	}

	private Map buildLimitParamData(AccessAuthorizeRequestWrapper accessAuthorizeRequestWrapper) {
		if (accessAuthorizeRequestWrapper != null) {
			Map pkeys = accessAuthorizeRequestWrapper.getParameterMap();
			Map datas = new HashMap(pkeys);
			Enumeration attrs = accessAuthorizeRequestWrapper.getAttributeNames();
			while (attrs.hasMoreElements()) {
				String attr = String.valueOf(attrs.nextElement());
				if (attr.startsWith("org.springframework")) {
					continue;
				}
				if (!datas.containsKey(attr)) {
					datas.put(attr, accessAuthorizeRequestWrapper.getAttribute(attr));
				}
			}
			Enumeration names = accessAuthorizeRequestWrapper.getHeaderNames();
			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				datas.put(name, accessAuthorizeRequestWrapper.getHeader(name));
			}
			return datas;
		}
		return null;
	}

	@Override
	public void init(FilterConfig filterConfig)
			throws ServletException {
	}

}
