package com.woniu.sncp.pay.core.service.payment.process;

import java.util.Map;

import org.springframework.dao.DataAccessException;

import com.woniu.sncp.pay.common.exception.OrderIsRefundException;
import com.woniu.sncp.pay.common.exception.OrderIsSuccessException;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;
import com.woniu.sncp.pay.core.service.payment.platform.AbstractPayment;

/**
 * 支付流程
 * @author luzz
 *
 */
public interface PaymentProcess {
	
	/**
	 * 实际支付币种 - 默认为人民币
	 */
	public static final String MONEY_CURRENCY = "R";

	/**
	 * 获取平台实际支付币种 - 默认为人民币 - "R"<br />
	 * 如果有其他币种，则具体平台需要覆写该方法
	 * 
	 * @return
	 */
	public String getMoneyCurrency();
	
	
	void doPay(Map<String, Object> inParams);
	
	Map<String, Object> doOrderCheck(String orderNo,Long merchantId);
	
	Map<String, Object> validateBackParams(AbstractPayment actualPayment,
			Map<String, Object> inParams) throws DataAccessException, ValidationException, OrderIsSuccessException,PaymentRedirectException, OrderIsRefundException;
	
	/**
	 * 未支付订单 交易关闭
	 * 
	 * @param orderNo
	 * @return
	 */
	Map<String,Object> cancelOrder(String orderNo,long merchantId,long platformId,Map<String,Object> extParams);
}
