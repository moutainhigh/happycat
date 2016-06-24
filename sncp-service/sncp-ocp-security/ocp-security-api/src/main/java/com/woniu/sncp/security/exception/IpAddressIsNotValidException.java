package com.woniu.sncp.security.exception;

import com.woniu.sncp.exception.web.WebBaseException;

public class IpAddressIsNotValidException extends WebBaseException {

	private static final long serialVersionUID = 1L;

	private static final String CODE = "610001";
	
	public IpAddressIsNotValidException(Object[] args) {
		super(CODE,args);
	}
}
