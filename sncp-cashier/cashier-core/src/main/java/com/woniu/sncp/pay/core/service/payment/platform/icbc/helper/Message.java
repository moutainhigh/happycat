package com.woniu.sncp.pay.core.service.payment.platform.icbc.helper;


public class Message {
	//required
	private String creditType = "1"; // 0仅允许使用借记卡支付  1仅信用卡支付  2 ALL
	private String notifyType = "HS"; //HS 在交易完成后实时通知  AG
	//options
	private int resultType = 1; // 0 1  银行只向商户发送交易成功的通知信息
	private String merReference = "";
	private String merCustomIp = "";
	private int goodsType = 1; //0虚拟 1 实物
	private String merCustomID = "";
	private String merCustomPhone = "";
	private String goodsAddress = "";
	private String merOrderRemark = "";
	private String merHint = "";
	private String remark1 = "";
	private String remark2 = "";
	private String merURL;
	private String merVAR = ""; //test
	public String getCreditType() {
		return creditType;
	}
	/**
	 * 0 仅允许使用借记卡支付  1 仅信用卡支付  2 ALL
	 * @param creditType
	 */
	public void setCreditType(String creditType) {
		this.creditType = creditType;
	}
	public String getNotifyType() {
		return notifyType;
	}
	public void setNotifyType(String notifyType) {
		this.notifyType = notifyType;
	}
	public int getResultType() {
		return resultType;
	}
	public void setResultType(int resultType) {
		this.resultType = resultType;
	}
	public String getMerReference() {
		return merReference;
	}
	public void setMerReference(String merReference) {
		this.merReference = merReference;
	}
	public String getMerCustomIp() {
		return merCustomIp;
	}
	public void setMerCustomIp(String merCustomIp) {
		this.merCustomIp = merCustomIp;
	}
	public int getGoodsType() {
		return goodsType;
	}
	public void setGoodsType(int goodsType) {
		this.goodsType = goodsType;
	}
	public String getMerCustomID() {
		return merCustomID;
	}
	public void setMerCustomID(String merCustomID) {
		this.merCustomID = merCustomID;
	}
	public String getMerCustomPhone() {
		return merCustomPhone;
	}
	public void setMerCustomPhone(String merCustomPhone) {
		this.merCustomPhone = merCustomPhone;
	}
	public String getGoodsAddress() {
		return goodsAddress;
	}
	public void setGoodsAddress(String goodsAddress) {
		this.goodsAddress = goodsAddress;
	}
	public String getMerOrderRemark() {
		return merOrderRemark;
	}
	public void setMerOrderRemark(String merOrderRemark) {
		this.merOrderRemark = merOrderRemark;
	}
	public String getMerHint() {
		return merHint;
	}
	public void setMerHint(String merHint) {
		this.merHint = merHint;
	}
	public String getRemark1() {
		return remark1;
	}
	public void setRemark1(String remark1) {
		this.remark1 = remark1;
	}
	public String getRemark2() {
		return remark2;
	}
	public void setRemark2(String remark2) {
		this.remark2 = remark2;
	}
	public String getMerURL() {
		return merURL;
	}
	public void setMerURL(String merURL) {
		this.merURL = merURL;
	}
	public String getMerVAR() {
		return merVAR;
	}
	public void setMerVAR(String merVAR) {
		this.merVAR = merVAR;
	}
}
