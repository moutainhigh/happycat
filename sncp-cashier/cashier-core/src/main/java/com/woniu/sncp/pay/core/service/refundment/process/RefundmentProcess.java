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
 * 退款流程
 * 
 * @author fuzl
 * 
 */
public interface RefundmentProcess {
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

	Map<String, Object> doOrderCheck(String orderNo);
	
	
	void doRefund(Map<String, Object> inParams);

	Map<String, Object> doBatchCheck(String pBatchNo);
	
	Map<String, Object> doBatchCheck(HttpServletRequest request,String pBatchNo);

	Map<String, Object> validateBackParams(AbstractPayment actualPayment,
			Map<String, Object> inParams) throws DataAccessException,
			ValidationException, OrderIsSuccessException,
			PaymentRedirectException, RefundBatchIsSuccessException;
}
