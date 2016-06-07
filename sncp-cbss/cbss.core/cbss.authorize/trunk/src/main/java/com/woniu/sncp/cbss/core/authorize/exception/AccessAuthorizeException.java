package com.woniu.sncp.cbss.core.authorize.exception;

public class AccessAuthorizeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2592455320025184450L;

	public AccessAuthorizeException(String exception) {
		super(exception);
	}

	public AccessAuthorizeException(Throwable throwable) {
		super(throwable);
	}

	public AccessAuthorizeException(String exception, Throwable throwable) {
		super(exception, throwable);
	}
}
