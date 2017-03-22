package com.woniu.sncp.pay.common.exception;


/**
 * 使用{@link Assert}校验，不合法则抛异常 该异常需单独捕获！因为它包含了具体的异常信息
 * 
 * @author yanghao
 * @since 2010-5-14
 * 
 */
public class ValidationException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;
	private String errorCode;

	public ValidationException() {
		super();
	}

	public ValidationException(String message) {
		super(message);
	}

	public ValidationException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public ValidationException(String errorCode, String message,Throwable cause) {
		super(message,cause);
		this.errorCode = errorCode;
	}

	public ValidationException(Throwable cause) {
		super(cause);
	}

	public ValidationException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
}
