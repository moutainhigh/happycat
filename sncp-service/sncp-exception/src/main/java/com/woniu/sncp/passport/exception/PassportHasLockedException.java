package com.woniu.sncp.passport.exception;

import com.woniu.sncp.exception.BusinessException;

/**
 * 帐号已经被锁定
 * @author chenyx
 * @date 2016年5月4日
 */
public class PassportHasLockedException extends BusinessException {

	private static final long serialVersionUID = -1882280459253119282L;

	public PassportHasLockedException() {
		super();
	}
	
	public PassportHasLockedException(String message) {
		super(message);
	}

}
