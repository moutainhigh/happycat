package cbss.core.authorize;

import java.io.IOException;
import java.util.List;

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

import cbss.core.authorize.exception.AccessAuthorizeException;
import cbss.core.authorize.exception.AccessLimitException;
import cbss.core.errorcode.EchoInfo;
import cbss.core.errorcode.ErrorCode;
import cbss.core.model.constant.NameFactory;
import cbss.core.model.request.Param;
import cbss.core.model.request.ParamValueValidateException;
import cbss.core.model.request.RequestClientInfo;
import cbss.core.model.request.RequestDatas;
import cbss.core.model.request.RequestParam;
import cbss.core.model.request.access.RequestAccess;

import com.alibaba.fastjson.JSONObject;

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

		// 1.封装请求对象
		AccessAuthorizeRequestWrapper accessAuthorizeRequestWrapper = new AccessAuthorizeRequestWrapper((HttpServletRequest) servletRequest);

		// 2.封装响应对象
		HttpServletResponseWrapper httpServletResponseWrapper = new HttpServletResponseWrapper((HttpServletResponse) servletResponse);

		// 3.校验请求头信息
		String headAccessverify = accessAuthorizeRequestWrapper.getHeader(NameFactory.request_head.accessverify.name());
		String headAccessId = accessAuthorizeRequestWrapper.getHeader(NameFactory.request_head.accessId.name());
		String headAccessType = accessAuthorizeRequestWrapper.getHeader(NameFactory.request_head.accessType.name());
		String headAccessPasswd = accessAuthorizeRequestWrapper.getHeader(NameFactory.request_head.accessPasswd.name());

		if (StringUtils.isBlank(headAccessverify)) {
			echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1).setData("HTTP HEADER accessverify IS MUST SET.")));
			return;
		}
		if (StringUtils.isBlank(headAccessId)) {
			echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1).setData("HTTP HEADER accessId IS MUST SET.")));
			return;
		}
		if (StringUtils.isBlank(headAccessType)) {
			echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1).setData("HTTP HEADER accessType IS MUST SET.")));
			return;
		}
		if (StringUtils.isBlank(headAccessPasswd)) {
			echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1).setData("HTTP HEADER accessPasswd IS MUST SET.")));
			return;
		}

		RequestAccess requestAccess = null;
		try {
			// 4.转换业务参数
			int index = accessUrlConfigurationProperties.getUrls().indexOf(accessAuthorizeRequestWrapper.getRequestURI());
			if (accessUrlConfigurationProperties.getParamTypes() == null) {
				echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1).setData("NOT FOUND PARAM-OBJECT URI:" + accessAuthorizeRequestWrapper.getRequestURI())));
				return;
			}
			RequestDatas<RequestParam> paramType = accessUrlConfigurationProperties.getParamObjects().get(index);
			if (null == paramType) {
				echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1).setData("NOT FOUND PARAM-OBJECT URI:" + accessAuthorizeRequestWrapper.getRequestURI())));
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
							echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1).setData("HTTP PARAM CHECK ERROR")));
							return;
						}
					} catch (ParamValueValidateException e) {
						echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1).setData("HTTP PARAM CHECK ERROR").setErrorInfo(e.getMessage())));
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
								echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1).setData("HTTP HEADER ACCESSVERIFY IS MUST SET.")));
								return;
							}
						} catch (ParamValueValidateException e) {
							echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1).setData("HTTP PARAM CHECK ERROR.").setErrorInfo(e.getMessage())));
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
						echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1).setData("HTTP PARAM RequestClientInfo's startReqTime Value is MUST SET.")));
						return;
					}
				} catch (Exception e) {
					echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1).setData("HTTP PARAM RequestClientInfo's startReqTime Value is MUST SET.").setErrorInfo(e.getMessage())));
					return;
				}
			}

			// 4.3
			if (null != requestDatas.getAccessSecurityInfo()) {
				echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1).setData("HTTP PARAM TOO MORE.1")));
				return;
			}
			if (null != requestDatas.getSecurityResource()) {
				echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1).setData("HTTP PARAM TOO MORE.2")));
				return;
			}

			// 5.封装校验数据
			requestAccess = new RequestAccess();
			requestAccess.setServletRequest(accessAuthorizeRequestWrapper);
			requestAccess.setBody(accessAuthorizeRequestWrapper.getBody());
			requestAccess.setRequestDatas(requestDatas);
			requestAccess.setAccessVerify(headAccessverify);
			if (requestAccess.getRequestDatas().getOther() != null && requestAccess.getRequestDatas().getOther().getOtherFirst() != null) {
				String traceState = ObjectUtils.toString((requestAccess.getRequestDatas().getOther().getOtherFirst().get(NameFactory.request_otherinfo.traceState.name())));
				requestDatas.setTraceState(traceState);
				requestAccess.setTraceState(traceState);
			}

			// 6.访问权限认证
			try {
				if (!accessAuthorize.authorize(requestAccess)) {
					echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1).setData("REQUEST AUTHORIZE FOBIDDEN.")));
					return;
				}
			} catch (AccessAuthorizeException e) {
				echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1).setData("REQUEST AUTHORIZE FOBIDDEN.").setErrorInfo(e.getMessage())));
				return;
			}

			// 7.访问频率限制
			try {
				if (accessAuthorize.limit(requestAccess)) {
					echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1).setData("REQUEST ACCESS FREQUENCY TOO MORE.")));
					return;
				}
			} catch (AccessLimitException e) {
				echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1).setData("REQUEST ACCESS FREQUENCY TOO MORE.").setErrorInfo(e.getMessage())));
				return;
			}

			if (requestAccess.getRequestDatas().getSecurityResource() != null
					&& requestAccess.getRequestDatas().getSecurityResource().getNoteFirst().containsKey(NameFactory.default_constant.ISSETSECURITYRESOURCES.getValue())) {
				// 8.重新封装业务数据将验证资源数据存入
				accessAuthorizeRequestWrapper.setBody(JSONObject.toJSONString(requestAccess.getRequestDatas()));
			}

			// 9.下一个过滤链
			filterChain.doFilter(accessAuthorizeRequestWrapper, httpServletResponseWrapper);

		} catch (Exception e) {
			logger.error("doFilter", e);
			echoInfo(httpServletResponseWrapper, (errorCode.getErrorCode(-1).setData("HTTP REQUEST ERROR").setErrorInfo(e.getMessage())));
			return;
		} finally {
			requestAccess = null;
			accessAuthorizeRequestWrapper.clear();
		}
	}

	@Override
	public void init(FilterConfig filterConfig)
			throws ServletException {
	}

}