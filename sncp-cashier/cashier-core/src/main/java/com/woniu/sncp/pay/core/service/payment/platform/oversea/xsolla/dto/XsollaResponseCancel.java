package com.woniu.sncp.pay.core.service.payment.platform.oversea.xsolla.dto;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlRootElement;
/**
 *  
 *  xsolla消息格式-cancel
 * 
 */
@XmlRootElement(name="response")
@XmlAccessorOrder(XmlAccessOrder.UNDEFINED)
public class XsollaResponseCancel extends XsollaResponseBase {

	@Override
	public String getComment() {
		if ("0".equals(getResult())) {
			return null;
		}
		return cancelComment.get(getResult());
	}
}
