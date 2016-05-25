package com.woniu.sncp.exception;

/**
 * 系统服务异常，所有系统异常全部继承于它
 * @author chenyx
 * @since JDK1.8
 * @version 1.0.0
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
