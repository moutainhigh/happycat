package com.woniu.sncp.cbss.core.errorcode;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class ErrorCode {

	@Autowired
	private MessageSource messageSource;

	private String translate(String code, Object[] args, Locale locale) {
		String errorMsg = messageSource.getMessage(code, args, locale);
		return errorMsg;
	}

	private String translate(String code) {
		String errorMsg = messageSource.getMessage(code, null, Locale.CHINA);
		return errorMsg;
	}

	public EchoInfo<Object> getErrorCode(int code, Object[] args, Locale locale) {
		String info = translate(String.valueOf(code), args, locale);
		EchoInfo<Object> echoInfo = new EchoInfo<Object>();
		echoInfo.setMsgcode(code);
		echoInfo.setMessage(info);
		return echoInfo;
	}

	public EchoInfo<Object> getErrorCode(int code, String requestUuid) {
		String info = translate(String.valueOf(code));
		EchoInfo<Object> echoInfo = new EchoInfo<Object>();
		echoInfo.setMsgcode(code);
		echoInfo.setMessage(info);
		echoInfo.setUuid(requestUuid);
		return echoInfo;
	}
}
