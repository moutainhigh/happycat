package com.woniu.sncp.exception;

/**
 * 业务异常父类
 * 所有业务异常全部继承于它
 * @author chenyx
 * @date 2016年5月6日
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
