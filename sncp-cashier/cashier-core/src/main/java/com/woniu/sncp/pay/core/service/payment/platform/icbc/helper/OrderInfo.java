package com.woniu.sncp.pay.core.service.payment.platform.icbc.helper;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OrderInfo {
	
	private String orderDate; //YYYYMMDDHHmmss
	//币种 目前只支持人民币
	private String curType = "001";
	//商户号
	private String merID;
	//最大长度5
	private List<SubOrderInfo> subOrderInfoList = new ArrayList<SubOrderInfo>(5);
	
	@XmlElement(required=true)
	public String getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}
	
	@XmlElement(required=true, defaultValue="001")
	public String getCurType() {
		return curType;
	}
	public void setCurType(String curType) {
		this.curType = curType;
	}
	public String getMerID() {
		return merID;
	}
	public void setMerID(String merID) {
		this.merID = merID;
	}
	
	@XmlElementWrapper(name="subOrderInfoList")
	@XmlElements(value={@XmlElement(name="subOrderInfo", type=SubOrderInfo.class)})
	public List<SubOrderInfo> getSubOrderInfoList() {
		return subOrderInfoList;
	}
	public void setSubOrderInfoList(List<SubOrderInfo> subOrderInfoList) {
		if (subOrderInfoList != null) {
			if (this.subOrderInfoList.size() + subOrderInfoList.size() <= 5) {
				this.subOrderInfoList.addAll(subOrderInfoList);
			}
		}
	}
	
	public void addSubOrderInfo(SubOrderInfo subOrder) {
		if (this.subOrderInfoList.size() < 5 && subOrder != null) {
			this.subOrderInfoList.add(subOrder);
		}
	}
	
	@XmlRootElement(name="subOrderInfo")
	public static class SubOrderInfo {
		
		//订单ID 长度30
		private String orderid;
		//金额 单位分
		private int amount;
		//分期 1默认直接支付  1、3、6、9、12、18、24
		private int installmentTimes; 
		//商户入账账号，只能交易时指定。
		private String merAcct;
		
		private String goodsID;
		private String goodsName;
		private Integer goodsNum;
		private String carriageAmt;
		//银行端指令流水号
		private String tranSerialNo;
		
		public String getOrderid() {
			return orderid;
		}
		public void setOrderid(String orderid) {
			this.orderid = orderid;
		}
		public int getAmount() {
			return amount;
		}
		public void setAmount(int amount) {
			this.amount = amount;
		}
		public int getInstallmentTimes() {
			return installmentTimes;
		}
		public void setInstallmentTimes(int installmentTimes) {
			this.installmentTimes = installmentTimes;
		}
		public String getMerAcct() {
			return merAcct;
		}
		public void setMerAcct(String merAcct) {
			this.merAcct = merAcct;
		}
		public String getGoodsID() {
			return goodsID;
		}
		public void setGoodsID(String goodsID) {
			this.goodsID = goodsID;
		}
		public String getGoodsName() {
			return goodsName;
		}
		public void setGoodsName(String goodsName) {
			this.goodsName = goodsName;
		}
		public Integer getGoodsNum() {
			return goodsNum;
		}
		public void setGoodsNum(Integer goodsNum) {
			this.goodsNum = goodsNum;
		}
		public String getCarriageAmt() {
			return carriageAmt;
		}
		public void setCarriageAmt(String carriageAmt) {
			this.carriageAmt = carriageAmt;
		}
		public String getTranSerialNo() {
			return tranSerialNo;
		}
		public void setTranSerialNo(String tranSerialNo) {
			this.tranSerialNo = tranSerialNo;
		}
	}
}
