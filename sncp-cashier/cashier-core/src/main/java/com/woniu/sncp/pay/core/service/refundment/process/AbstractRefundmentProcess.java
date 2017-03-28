package com.woniu.sncp.pay.core.service.refundment.process;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.dao.DataAccessException;

import com.woniu.sncp.pay.common.exception.OrderIsSuccessException;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.RefundBatchIsSuccessException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;

/**
 * <p>descrption: 
 *  * 抽象退款平台业务类，主要职责如下：<br />
 * 1.完成各平台独立的基本方法<br />
 * 2.调用各平台基本方法实现我方公共的框架方法，如：构建退款请求、请求退款等
 * </p>
 * @author fuzl
 * @date   2015年10月9日
 * @Copyright 2015 Snail Soft, Inc. All rights reserved.
 * 
 */
public class AbstractRefundmentProcess implements RefundmentProcess {

	/* (non-Javadoc)
	 * @see com.woniu.pay.core.refundment.process.RefundmentProcess#getMoneyCurrency()
	 */
	@Override
	public String getMoneyCurrency() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> doOrderCheck(String orderNo) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	/* (non-Javadoc)
	 * @see com.woniu.pay.core.refundment.process.RefundmentProcess#doRefund(java.util.Map)
	 */
	@Override
	public void doRefund(Map<String, Object> inParams) {
		// TODO Auto-generated method stub

	}
	/**
	 * 退款批次单校验
	 * @param pBatchNo
	 * @return
	 */
	public Map<String, Object> doBatchCheck(String pBatchNo) {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.woniu.pay.core.refundment.process.RefundmentProcess#validateBackParams(com.woniu.pay.core.payment.platform.AbstractPayment, java.util.Map)
	 */
	@Override
	public Map<String, Object> validateBackParams(
			AbstractPayment actualPayment, Map<String, Object> inParams)
			throws DataAccessException, ValidationException,
			OrderIsSuccessException, PaymentRedirectException, RefundBatchIsSuccessException{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> doBatchCheck(HttpServletRequest request,
			String pBatchNo) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
