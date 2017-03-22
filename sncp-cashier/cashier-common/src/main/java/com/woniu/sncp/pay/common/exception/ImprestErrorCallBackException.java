package com.woniu.sncp.pay.common.exception;

/**
 * 回调结果是支付、直充失败
 */
public class ImprestErrorCallBackException extends CallBackException {

	private static final long serialVersionUID = 1L;

	public ImprestErrorCallBackException() {
		super();
	}

	public ImprestErrorCallBackException(String platomformId, String platomformName, Object param,String state,String orderNo) {
		super(platomformId, platomformName, "回调结果是支付、话费、流量处理失败", param, state,orderNo);
	}

}
