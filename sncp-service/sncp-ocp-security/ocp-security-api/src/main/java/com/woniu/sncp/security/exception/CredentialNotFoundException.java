package com.woniu.sncp.security.exception;

import com.woniu.sncp.exception.BusinessException;
import com.woniu.sncp.exception.web.WebBaseException;

/**
 * 认证异常
 * @author chenyx
 * @since JDK 1.8
 */
public class CredentialNotFoundException extends WebBaseException {

	private static final long serialVersionUID = 1L;

	private static final String CODE = "610008";
	
	public CredentialNotFoundException(Object[] args) {
		super(CODE,args);
	}
}
