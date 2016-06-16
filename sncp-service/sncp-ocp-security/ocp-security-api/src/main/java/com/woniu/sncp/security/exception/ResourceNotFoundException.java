package com.woniu.sncp.security.exception;

import com.woniu.sncp.exception.BusinessException;

public class ResourceNotFoundException  extends BusinessException {

	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException() {
		super();
	}
	
	public ResourceNotFoundException(String message) {
		super(message);
	}
}
