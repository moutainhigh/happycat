package com.woniu.sncp.pay.common.exception;


/**
 * 此类用于退款强制转向时使用，不是异常
 * @author fuzl
 *
 */
public class RefundmentRedirectException extends Exception {

	private static final long serialVersionUID = 1L;

	public RefundmentRedirectException() {
		super();
	}

	public RefundmentRedirectException(String message) {
		super(message);
	}

	public RefundmentRedirectException(Throwable cause) {
		super(cause);
	}

	public RefundmentRedirectException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
