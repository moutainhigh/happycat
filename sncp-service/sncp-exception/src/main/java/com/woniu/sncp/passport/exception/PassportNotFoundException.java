package com.woniu.sncp.passport.exception;

import com.woniu.sncp.exception.BusinessException;

/**
 * 帐号未找到异常
 * @author chenyx
 * @date 2016年5月4日
 */
public class PassportNotFoundException extends BusinessException {

	private static final long serialVersionUID = 1L;
	
	public PassportNotFoundException() {
		super();
	}
	
	public PassportNotFoundException(String message) {
		super(message);
	}

}
