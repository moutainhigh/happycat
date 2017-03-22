package com.woniu.sncp.pay.common.exception;


/**
 * 
 * <p>descrption: 订单成功退款</p>
 * 
 * @author fuzl
 * @date   2016年9月9日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public class OrderIsRefundException extends Exception {

	private static final long serialVersionUID = 1L;

	public OrderIsRefundException() {
		super();
	}

	public OrderIsRefundException(String message) {
		super(message);
	}

	public OrderIsRefundException(Throwable cause) {
		super(cause);
	}

	public OrderIsRefundException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
