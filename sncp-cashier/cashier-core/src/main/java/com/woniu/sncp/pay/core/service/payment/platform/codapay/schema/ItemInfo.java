package com.woniu.sncp.pay.core.service.payment.platform.codapay.schema;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ItemInfo implements Serializable {
	private static final long serialVersionUID = 201203021L;

	private String code;
	private String name;
	private double price = 0;
	private short type = 0;

	public short getType() {
		return type;
	}
	public void setType(short type) {
		this.type = type;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
}
