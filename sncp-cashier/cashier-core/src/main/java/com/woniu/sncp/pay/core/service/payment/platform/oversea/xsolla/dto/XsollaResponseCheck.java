package com.woniu.sncp.pay.core.service.payment.platform.oversea.xsolla.dto;

import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlRootElement;
/**
 *  
 *  xsolla消息格式-check
 * 
 */
@XmlRootElement(name="response")
public class XsollaResponseCheck extends XsollaResponseBase {

	private Specification specification;
	
	public Specification getSpecification() {
		return specification;
	}

	public void setSpecification(Specification specification) {
		this.specification = specification;
	}
	
	@Override
	public String getComment() {
		if ("0".equals(getResult())) {
			return null;
		}
		return checkComment.get(getResult());
	}
	
	/**
	 * 
	 * @author caowl
	 *
	 */
	@XmlRootElement
	@XmlAccessorOrder
	public static class Specification {
		
		/** 注册时间 **/
		private String date;
		/**在线时长**/
		private String hours;
		/**交易记录**/
		private String trade;
		
		public String getDate() {
			return date;
		}
		
		public void setDate(String date) {
			this.date = date;
		}
		
		public String getHours() {
			return hours;
		}
		
		public void setHours(String hours) {
			this.hours = hours;
		}
		
		public String getTrade() {
			return trade;
		}
		
		public void setTrade(String trade) {
			this.trade = trade;
		}
	}
}
