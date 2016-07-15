package com.woniu.sncp.cbss.core.authorize.rest;

import java.util.Date;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.woniu.sncp.cbss.core.errorcode.EchoInfo;
import com.woniu.sncp.cbss.core.model.request.RequestClientInfo;
import com.woniu.sncp.cbss.core.model.request.RequestDatas;
import com.woniu.sncp.cbss.core.trace.aspect.listener.Trace;

@Aspect
@Component
public class RestControllerAspect {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private Trace trace;

	@Around("@annotation(echoRestControllerAspectType)")
	public Object around(ProceedingJoinPoint joinPoint, EchoRestControllerAspectType echoRestControllerAspectType)
			throws Throwable {
		Object retValue = null;

		RequestDatas<?> requestDatas = null;

		try {
			Object[] args = joinPoint.getArgs();
			for (Object arg : args) {
				if (arg instanceof RequestDatas) {
					requestDatas = (RequestDatas<?>) arg;
					break;
				}
			}

			retValue = joinPoint.proceed();

			if (retValue instanceof EchoInfo<?>) {
				EchoInfo<?> tmp = ((EchoInfo<?>) retValue);
				tmp.setUuid(requestDatas.getSessionId());
				tmp.setNextSignType(requestDatas.getSecurityResource().getSignType());
				tmp.setAppRspTime(System.currentTimeMillis());
				tmp.setServerState(Integer.parseInt(requestDatas.getSecurityResource().getState()));
				tmp.setDomaneName(requestDatas.getSecurityResource().getDomaneName());
				tmp.setFutureTime(requestDatas.getSecurityResource().getFutureTime());
			}

			return retValue;

		} catch (Throwable e) {
			logger.error("RestController", e);

			EchoInfo<String> rEchoInfo = new EchoInfo<String>();
			rEchoInfo.setMessage(e.getMessage());
			rEchoInfo.setMsgcode(EchoInfo.ERROR);
			rEchoInfo.setData("");
			rEchoInfo.setUrl(requestDatas.getSecurityResource().getId().getUrl());

			rEchoInfo.setUuid(requestDatas.getSessionId());
			rEchoInfo.setNextSignType(-1);
			rEchoInfo.setAppRspTime(System.currentTimeMillis());
			rEchoInfo.setServerState(Integer.parseInt(requestDatas.getSecurityResource().getState()));
			rEchoInfo.setDomaneName(requestDatas.getSecurityResource().getDomaneName());
			rEchoInfo.setFutureTime(requestDatas.getSecurityResource().getFutureTime());

			retValue = rEchoInfo;

			return retValue;

		} finally {
			try {
				if (requestDatas != null && requestDatas.getClientInfo() != null) {
					// url,入参,请求时间,接到时间,接到前网络消耗时间,处理结束时间,接到到处理之间的时间
					List<RequestClientInfo> clinfos = requestDatas.getClientInfo();
					for (RequestClientInfo requestClientInfo : clinfos) {
						trace.traceApiTime(requestDatas.getSecurityResource().getId().getUrl(), requestDatas, requestClientInfo.getStartReqTime(), new Date(requestDatas.getReciveTime()), new Date(),
								new Date(requestDatas.getAccessAuthorizeEndtime()), retValue);
					}
				} else {
					trace.traceApiTime(requestDatas.getSecurityResource().getId().getUrl(), requestDatas, null, new Date(requestDatas.getReciveTime()), new Date(),
							new Date(requestDatas.getAccessAuthorizeEndtime()), retValue);
				}
			} catch (Exception e) {
				logger.error("traceApiTime", e);
			}
		}
	}
}
