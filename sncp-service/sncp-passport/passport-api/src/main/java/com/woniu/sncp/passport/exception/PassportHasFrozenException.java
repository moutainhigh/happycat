package com.woniu.sncp.passport.exception;

/**
 * 帐号已经被冻结
 * @author chenyx
 * @date 2016年5月4日
 */
public class PassportHasFrozenException extends RuntimeException {

	private static final long serialVersionUID = 4932097301321177237L;

	public PassportHasFrozenException() {
		super();
	}
	
	public PassportHasFrozenException(String message) {
		super(message);
	}
	
	

	
}
