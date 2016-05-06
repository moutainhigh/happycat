package com.woniu.sncp.exception;

/**
 * 系统服务异常，所有系统异常全部继承于它
 * @author chenyx
 * @date 2016年5月6日
 */
public class SystemException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public SystemException() {
		super();
	}
	
	public SystemException(String message) {
		super(message);
	}
}
