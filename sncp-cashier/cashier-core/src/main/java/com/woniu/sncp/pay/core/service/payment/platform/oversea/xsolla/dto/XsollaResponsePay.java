package com.woniu.sncp.pay.core.service.payment.platform.oversea.xsolla.dto;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *  
 *  xsolla消息格式-pay
 * 
 */
@XmlRootElement(name="response")
@XmlAccessorOrder(XmlAccessOrder.UNDEFINED)
public class XsollaResponsePay extends XsollaResponseBase {
	
	private String id;
	private String id_shop;
	private String sum;
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId_shop() {
		return id_shop;
	}

	public void setId_shop(String id_shop) {
		this.id_shop = id_shop;
	}

	public String getSum() {
		return sum;
	}

	public void setSum(String sum) {
		this.sum = sum;
	}
	
	@Override
	public String getComment() {
		if ("0".equals(getResult())) {
			return null;
		}
		return payComment.get(getResult());
	}
}
