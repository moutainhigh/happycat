package com.woniu.sncp.pay.core.service;

import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.Payment;

/**
 * 
 * <p>descrption: 退款抽象平台服务类</p>
 * 
 * @author fuzl
 * @date   2015年10月9日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 */
public interface RefundmentService {


	/**
	 * 根据抽象支付平台从配置中获取平台ID
	 * 
	 * @param actualImprestPayment
	 * @return
	 */
	public long findRefundmentIdByPayment(Payment actualPayment);
	
    /**
     * 根据平台ID获得spring中的平台
     *
     * @param refundmentId
     *            支付平台ID
     * @throws IllegalArgumentException
     */
    public AbstractPayment findPaymentById(long refundmentId) throws IllegalArgumentException;

    
    /**
	 * 异常告警处理
	 * 
	 * @param exception
	 */
	public void monitorExcetpionToAlter(Exception exception);
}