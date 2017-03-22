package com.woniu.sncp.pay.common.exception;


/**
 * 
 * <p>descrption:异常类：批次单退款成功,应返回给对方成功信息 </p>
 * 
 * @author fuzl
 * @date   2015年10月10日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public class RefundBatchIsSuccessException extends Exception {

	private static final long serialVersionUID = 1L;

	public RefundBatchIsSuccessException() {
		super();
	}

	public RefundBatchIsSuccessException(String message) {
		super(message);
	}

	public RefundBatchIsSuccessException(Throwable cause) {
		super(cause);
	}

	public RefundBatchIsSuccessException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
