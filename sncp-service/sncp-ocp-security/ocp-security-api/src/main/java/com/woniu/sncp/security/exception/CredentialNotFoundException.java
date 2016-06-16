package com.woniu.sncp.security.exception;

import com.woniu.sncp.exception.BusinessException;

/**
 * 认证异常
 * @author chenyx
 * @since JDK 1.8
 */
public class CredentialNotFoundException extends BusinessException {

	private static final long serialVersionUID = 1L;

	public CredentialNotFoundException() {
		super();
	}
	
	public CredentialNotFoundException(String message) {
		super(message);
	}
}
