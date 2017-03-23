package com.woniu.sncp.pay.core.service.payment.platform;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.dao.DataAccessException;

import com.woniu.pay.pojo.Platform;
import com.woniu.sncp.pay.common.exception.PaymentRedirectException;
import com.woniu.sncp.pay.common.exception.ValidationException;

/**
 * 
 * 支付渠道接口
 * 
 */
public interface Payment {
	
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
	
	/**
	 * 生成订单 - 步骤0 - 根据订单返回提交给支付平台的参数<br />
	 * 第三方平台充值 - 返回Map中必须包含payUrl - 提交给支付平台的URL
	 * 
	 * @param inParams
	 *            inParams[imprestOrder,cardType,paymentPlatform,productName,
	 *            paymentProcductType]
	 */
	Map<String, Object> orderedParams(Map<String, Object> inParams) throws ValidationException;
	

	/**
	 * 后台处理 - 步骤1 - 验证后台参数<br />
	 * 1.参数加密校验<br />
	 * 2.我们提交的参数原样返回校验 (orderNo + key MD5值)<br />
	 * 3.设置支付模式<br />
	 * 4.判断支付状态<br />
	 * 如果都正常，返回 ImprestOrder(官方)以及调用过程需要的相关参数进行下一步充值，同时返回平台支付状态
	 * 
	 * @param request
	 * @return 1.ImprestOrder(官方)<br />
	 *         2.对方传过来的支付金额(Integer)，必须精确到分<br />
	 *         2.调用过程需要的相关参数<br />
	 *         3.同时返回平台支付状态
	 * @throws ImprestRedirectException
	 */
	Map<String, Object> validateBackParams(HttpServletRequest request, Platform platform)
			throws ValidationException, DataAccessException,PaymentRedirectException;
	
	
	/**
	 * 订单验证 - 步骤9 - 到对方平台中查看我方订单是否已支付
	 * 
	 * @param inParams
	 *            inParams
	 * @return A-支付状态:<br />
	 *         1 - {@link ImprestConstant#PAYMENT_STATE_PAYED}<br />
	 *         2 - {@link ImprestConstant#PAYMENT_STATE_FAILED} <br />
	 *         3 - {@link ImprestConstant#PAYMENT_STATE_NOPAYED} <br />
	 *         4 - {@link ImprestConstant#PAYMENT_STATE_NOPAYED}<br />
	 *         B - 对方订单号oppositeOrderNo <br />
	 *         C - 对方支付金额，必须精确到分
	 */
	Map<String, Object> checkOrderIsPayed(Map<String, Object> inParams);
	 
	/**
	 * 第三方支付平台通讯加密，各平台加密方式不一致
	 * 
	 * @param source
	 *            源字符串
	 */
	String encode(Map<String, Object> inParams) throws ValidationException;

	/**
	 * 后台处理 - 步骤3 - 返回给支付平台的通知
	 * 
	 * @param request
	 *            servletRequest
	 * @param response
	 *            servletResponse
	 * @param isImprestedSuccess
	 *            是否充值成功 false - 失败 true - 成功
	 */
	void paymentReturn(Map<String, Object> inParams, HttpServletResponse response,
			boolean isImprestedSuccess);

	/**
	 * 各平台从第三方平台返回的request中获取订单号,主要用于前台返回
	 * 
	 * @param request
	 */
	String getOrderNoFromRequest(HttpServletRequest request); 
	
	/**
	 * 取消订单
	 * 
	 * @param inParams
	 * @return
	 */
	Map<String, Object> cancelOrder(Map<String, Object> inParams);
}
