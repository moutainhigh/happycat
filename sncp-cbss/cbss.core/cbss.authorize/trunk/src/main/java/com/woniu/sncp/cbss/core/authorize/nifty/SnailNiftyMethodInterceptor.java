package com.woniu.sncp.cbss.core.authorize.nifty;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.facebook.nifty.core.RequestContexts;
import com.woniu.sncp.cbss.api.core.thrift.Access;
import com.woniu.sncp.cbss.api.core.thrift.ApiConstants;
import com.woniu.sncp.cbss.api.core.thrift.Data;
import com.woniu.sncp.cbss.api.core.thrift.Echo;
import com.woniu.sncp.cbss.api.core.thrift.Param;
import com.woniu.sncp.cbss.api.core.thrift.Signature;
import com.woniu.sncp.cbss.api.core.thrift.State;
import com.woniu.sncp.cbss.api.core.thrift.Status;
import com.woniu.sncp.cbss.core.authorize.AccessAuthorize;
import com.woniu.sncp.cbss.core.authorize.exception.AccessAuthorizeException;
import com.woniu.sncp.cbss.core.model.request.RequestClientInfo;
import com.woniu.sncp.cbss.core.model.request.RequestDatas;
import com.woniu.sncp.cbss.core.model.request.access.RequestAccess;
import com.woniu.sncp.cbss.core.model.request.nifty.SnailNiftyParam;
import com.woniu.sncp.cbss.core.trace.aspect.listener.Trace;

@Component
public class SnailNiftyMethodInterceptor implements MethodInterceptor {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private AccessAuthorize accessAuthorize;
	@Autowired
	private Trace trace;

	@Override
	public Object invoke(MethodInvocation mi)
			throws Throwable {
		RequestAccess requestAccess = null;
		Date accessAuthorizeEndtime = null;
		try {
			try {
				Access access = (Access) mi.getArguments()[0];
				Data data = (Data) mi.getArguments()[1];
				Signature signature = (Signature) mi.getArguments()[2];
				requestAccess = buildRequestAccess(mi.getMethod(), access, data, signature);

				authorize(requestAccess);
			} finally {
				accessAuthorizeEndtime = new Date();
			}
			Object rtn = mi.proceed();
			if (rtn instanceof Echo) {
				((Echo) rtn).setUuid(requestAccess.getSessionId());
				((Echo) rtn).setTime(System.currentTimeMillis());
				((Echo) rtn).setServerState(new State(Status.ALIVE, 0));
			}
			return rtn;
		} catch (Exception e) {
			if (e instanceof AccessAuthorizeException) {
				Echo echo = new Echo();
				echo.setMessage(e.getMessage() == null ? "NA1" : e.getMessage());
				echo.setUuid(requestAccess.getSessionId());
				echo.setData("NA1");
				echo.setTime(System.currentTimeMillis());
				echo.setNextSignType(ApiConstants.SIGNATURE_TYPE_DEFAULT);
				echo.setServerState(new State(Status.ALIVE, -1));
				echo.setMsgcode(-1);
				return echo;
			} else {
				Echo echo = new Echo();
				echo.setMessage(e.getMessage() == null ? "NA2" : e.getMessage());
				echo.setUuid(requestAccess.getSessionId());
				echo.setData("NA2");
				echo.setTime(System.currentTimeMillis());
				echo.setNextSignType(ApiConstants.SIGNATURE_TYPE_DEFAULT);
				echo.setServerState(new State(Status.ALIVE, -1));
				echo.setMsgcode(-2);
				return echo;
			}
		} finally {
			try {
				if (requestAccess != null && requestAccess.getRequestDatas() != null && requestAccess.getRequestDatas().getClientInfo() != null) {
					// url,入参,请求时间,接到时间,接到前网络消耗时间,处理结束时间,接到到处理之间的时间
					List<RequestClientInfo> clinfos = requestAccess.getRequestDatas().getClientInfo();
					for (RequestClientInfo requestClientInfo : clinfos) {
						trace.traceApiTime(requestAccess.getRequestURI(), requestAccess, requestClientInfo.getStartReqTime(), requestAccess.getReciveTime(), new Date(), accessAuthorizeEndtime);
					}
				} else {
					trace.traceApiTime(requestAccess.getRequestURI(), requestAccess, null, requestAccess.getReciveTime(), new Date(), accessAuthorizeEndtime);
				}
			} catch (Exception e) {
				logger.error("traceApiTime", e);
			}
			requestAccess = null;
		}
	}

	private void authorize(RequestAccess requestAccess) {
		if (!accessAuthorize.authorize(requestAccess)) {
			throw new AccessAuthorizeException("REQ ACCESS AUTHORIZE FORBIDDEN");
		}
		if (accessAuthorize.limit(requestAccess)) {
			throw new AccessAuthorizeException("REQ LIMIT AUTHORIZE FORBIDDEN");
		}
	}

	private RequestAccess buildRequestAccess(Method method, Access access, Data data, Signature signature) {
		RequestAccess requestAccess = new RequestAccess();
		requestAccess.setRequestParamData(new HashMap(0));
		requestAccess.setSessionId(UUID.randomUUID().toString());
		requestAccess.setTraceState(data.getTraceState());

		data.setSessionId(requestAccess.getSessionId());

		RequestDatas<SnailNiftyParam> requestDatas = new RequestDatas<SnailNiftyParam>();
		List<RequestClientInfo> clientInfos = new ArrayList<RequestClientInfo>();
		RequestClientInfo requestClientInfo = new RequestClientInfo();
		requestAccess.setRequestDatas(requestDatas);
		requestDatas.setClientInfo(clientInfos);
		clientInfos.add(requestClientInfo);

		requestDatas.setAccessId(access.getId());
		requestDatas.setAccessPasswd(access.getPasswd());
		requestDatas.setAccessType(access.getType());
		requestDatas.setVersion(data.getVersion());

		requestClientInfo.setClientUserIp(data.getClientRequest().getClientUserIp());
		requestClientInfo.setLocalReqIp(data.getClientRequest().getLocalReqIp());
		requestClientInfo.setStartReqTime(data.getClientRequest().getTime());

		Param param = data.getParam();

		requestAccess.setRequestURI(method.getDeclaringClass().getName() + "." + method.getName() + "." + param.getClassname());

		requestAccess.setAccessVerify(signature.getSignature());
		requestAccess.setAccessVerifyType(signature.getType());

		String body;
		if (data.getParam().getParamsSize() > 0) {
			body = JSONObject.toJSONString(param.getParams());
		} else {
			body = param.getParam();
		}

		if (param.getResolveType() == ApiConstants.PARAM_RESOLVE_TYPE_DEFAULT && body.startsWith("{") && body.endsWith("}")) {
			requestAccess.setBody(body);
		} else {
			throw new AccessAuthorizeException(String.format("ParamString [%s] is not FORMAT on [%s]", body, String.valueOf(param.getResolveType())));
		}

		SocketAddress remote = RequestContexts.getCurrentContext().getConnectionContext().getRemoteAddress();
		requestAccess.setRemoteIp(((InetSocketAddress) remote).getAddress().getHostAddress());
		return requestAccess;
	}
}
