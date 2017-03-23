package com.woniu.sncp.pay.core.service;

import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;
import com.woniu.sncp.pay.core.service.payment.platform.Payment;
import com.woniu.sncp.pay.core.transfer.platform.AbstractTransfer;

/**
 * 支付抽象平台服务类 
 * 
 * 
 *
 */
public interface PaymentService {


	/**
	 * 根据抽象支付平台从配置中获取平台ID
	 * 
	 * @param actualImprestPayment
	 * @return
	 */
	public long findPaymentIdByPayment(Payment actualPayment);
	
    /**
     * 根据平台ID获得spring中的平台
     *
     * @param paymentId
     *            支付平台ID
     * @throws IllegalArgumentException
     */
    public AbstractPayment findPaymentById(long paymentId) throws IllegalArgumentException;
    
    /**
     * 根据平台ID获得spring中的平台
     *
     * @param paymentId
     *            支付平台ID
     * @throws IllegalArgumentException
     */
    public AbstractTransfer findTransferById(long paymentId) throws IllegalArgumentException;


}