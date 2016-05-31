package com.woniu.sncp.exception;

/**
 * 业务异常父类
 * 所有业务异常全部继承于它
 * @author chenyx
 * @since JDK1.8
 * @version 1.0.0
 */
public class BusinessException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public BusinessException() {
		super();
	}
	
	public BusinessException(String message) {
		super(message);
	}
	
}
