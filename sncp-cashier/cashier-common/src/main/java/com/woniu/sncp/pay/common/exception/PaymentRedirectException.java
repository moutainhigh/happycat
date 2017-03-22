package com.woniu.sncp.pay.common.exception;


/**
 * 此类用于充值强制转向时使用，不是异常
 * @author yanghao
 * @since 2010-5-31 
 *
 */
public class PaymentRedirectException extends Exception {

	private static final long serialVersionUID = 1L;

	public PaymentRedirectException() {
		super();
	}

	public PaymentRedirectException(String message) {
		super(message);
	}

	public PaymentRedirectException(Throwable cause) {
		super(cause);
	}

	public PaymentRedirectException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
