package com.woniu.sncp.security.exception;

import com.woniu.sncp.exception.BusinessException;
import com.woniu.sncp.exception.web.WebBaseException;

public class ResourceNotFoundException extends WebBaseException {

	private static final long serialVersionUID = 1L;

	private static final String CODE = "610000";
	
	public ResourceNotFoundException(Object[] args) {
		super(CODE,args);
	}
}
