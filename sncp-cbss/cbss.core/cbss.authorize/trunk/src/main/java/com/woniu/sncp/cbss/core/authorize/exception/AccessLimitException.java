package com.woniu.sncp.cbss.core.authorize.exception;

public class AccessLimitException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AccessLimitException(String exception) {
		super(exception);
	}

	public AccessLimitException(Throwable throwable) {
		super(throwable);
	}

	public AccessLimitException(String exception, Throwable throwable) {
		super(exception, throwable);
	}

}
