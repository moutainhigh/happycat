package com.woniu.sncp.pay.core.service.payment.platform.paypal;

/**
 * paypal异常
 * @author Caowl
 *
 */
public class PaypalPaymentException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7537774285649835322L;
	
	public PaypalPaymentException(String msg){
		super(msg);
	}
	public PaypalPaymentException(String msg,Throwable t){
		super(msg, t);
	}
}
