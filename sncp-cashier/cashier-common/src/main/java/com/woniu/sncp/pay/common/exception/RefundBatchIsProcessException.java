package com.woniu.sncp.pay.common.exception;


/**
 * 
 * <p>descrption:异常类：批次单退款处理中,应返回给对方处理中信息 </p>
 * 
 * @author fuzl
 * @date   2016年10月24日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public class RefundBatchIsProcessException extends Exception {

	private static final long serialVersionUID = 1L;

	public RefundBatchIsProcessException() {
		super();
	}

	public RefundBatchIsProcessException(String message) {
		super(message);
	}

	public RefundBatchIsProcessException(Throwable cause) {
		super(cause);
	}

	public RefundBatchIsProcessException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
