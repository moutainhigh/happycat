package com.woniu.sncp.pay.core.service.payment.platform.icbc.helper;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="B2CRes")
public class B2CRes {

	private String interfaceName;
	private String interfaceVersion;
	private OrderInfo orderInfo;
	private Custom custom;
	private BankInfo bank;
	
	@XmlElement(required=true)
	public String getInterfaceName() {
		return interfaceName;
	}
	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}
	@XmlElement(required=true)
	public String getInterfaceVersion() {
		return interfaceVersion;
	}
	public void setInterfaceVersion(String interfaceVersion) {
		this.interfaceVersion = interfaceVersion;
	}
	@XmlElement(required=true)
	public OrderInfo getOrderInfo() {
		return orderInfo;
	}
	public void setOrderInfo(OrderInfo orderInfo) {
		this.orderInfo = orderInfo;
	}
	@XmlElement(required=true)
	public Custom getCustom() {
		return custom;
	}
	public void setCustom(Custom custom) {
		this.custom = custom;
	}
	@XmlElement(required=true)
	public BankInfo getBank() {
		return bank;
	}
	public void setBank(BankInfo bank) {
		this.bank = bank;
	}
}
