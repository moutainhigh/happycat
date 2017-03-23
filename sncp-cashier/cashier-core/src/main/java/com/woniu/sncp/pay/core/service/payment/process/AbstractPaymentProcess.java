package com.woniu.sncp.pay.core.service.payment.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象支付平台业务类，主要职责如下：<br />
 * 1.完成各平台独立的基本方法<br />
 * 2.调用各平台基本方法实现我方公共的框架方法，如：生成订单、完成支付等
 * 
 */
public abstract class AbstractPaymentProcess implements PaymentProcess {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 获取平台实际支付币种 - 默认为人民币 - "R"<br />
	 * 如果有其他币种，则具体平台需要覆写该方法
	 * 
	 * @return
	 */
	@Override
	public String getMoneyCurrency() {
		return MONEY_CURRENCY;
	}
}