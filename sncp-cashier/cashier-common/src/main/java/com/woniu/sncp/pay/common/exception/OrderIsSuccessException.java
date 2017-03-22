package com.woniu.sncp.pay.common.exception;


/**
 * 异常类：订单已充值成功,应返回给对方成功信息
 * @author yanghao
 * @since 2010-5-31 
 *
 */
public class OrderIsSuccessException extends Exception {

	private static final long serialVersionUID = 1L;

	public OrderIsSuccessException() {
		super();
	}

	public OrderIsSuccessException(String message) {
		super(message);
	}

	public OrderIsSuccessException(Throwable cause) {
		super(cause);
	}

	public OrderIsSuccessException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
