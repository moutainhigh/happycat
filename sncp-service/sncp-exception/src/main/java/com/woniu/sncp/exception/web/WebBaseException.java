package com.woniu.sncp.exception.web;

/**
 * Web服务接口异常
 * @author chenyx
 * @since JDK1.8
 * @version 1.0.0
 */
public class WebBaseException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	//异常编码
	private String code;
	
	//异常消息
	private Object[] args;
	
	public WebBaseException() {
		super();
	}

	public WebBaseException(String code, Object[] args) {
		super();
		this.code = code;
		this.args = args;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}
}
