package com.woniu.sncp.passport.exception;

import com.woniu.sncp.exception.BusinessException;

/**
 * 帐号已经被冻结
 * @author chenyx
 * @since JDK1.8
 * @version 1.0.0
 */
public class PassportHasFrozenException extends BusinessException {

	private static final long serialVersionUID = 4932097301321177237L;

	public PassportHasFrozenException() {
		super();
	}
	
	public PassportHasFrozenException(String message) {
		super(message);
	}
	
	

	
}
